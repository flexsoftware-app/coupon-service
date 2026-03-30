package pl.kkaczynski.coupon_service;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.out.GeoIpService;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class CouponServiceIntegrationTest {

	private static final String EMPIK_COUPON = "EMPIKREADS";
	private static final String EMPIK_USER = "empik-klient-42";
	private static final String POLISH_CLIENT_IP = "1.2.3.4";
	private static final String FOREIGN_CLIENT_IP = "9.9.9.9";

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("coupon_service_test")
			.withUsername("coupon")
			.withPassword("coupon");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		registry.add("spring.flyway.enabled", () -> "false");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private GeoIpService geoIpService;

	@Test
	void shouldCreateAndRedeemCouponEndToEnd() throws Exception {
		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", EMPIK_COUPON.toLowerCase(), "maxRedemptions", 2, "countryCode", "pl"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value(EMPIK_COUPON));

		when(geoIpService.resolveCountry(POLISH_CLIENT_IP)).thenReturn(CountryCode.of("PL"));

		mockMvc.perform(post("/api/coupons/" + EMPIK_COUPON.toLowerCase() + "/redemptions")
				.header("X-Forwarded-For", POLISH_CLIENT_IP)
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("userId", EMPIK_USER))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(EMPIK_USER))
				.andExpect(jsonPath("$.currentRedemptions").value(1))
				.andExpect(jsonPath("$.countryCode").value("PL"));
	}

	@Test
	void shouldRejectDuplicateCouponIgnoringCaseEndToEnd() throws Exception {
		String duplicateCoupon = "EMPIKDUPLICATE";

		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", duplicateCoupon, "maxRedemptions", 2, "countryCode", "PL"))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", duplicateCoupon.toLowerCase(), "maxRedemptions", 2, "countryCode", "PL"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Coupon with code '" + duplicateCoupon + "' already exists"));
	}

	@Test
	void shouldRejectRedemptionWhenCouponLimitIsReached() throws Exception {
		String limitedCoupon = "EMPIKLIMIT1";
		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", limitedCoupon, "maxRedemptions", 1, "countryCode", "PL"))))
				.andExpect(status().isCreated());

		when(geoIpService.resolveCountry(POLISH_CLIENT_IP)).thenReturn(CountryCode.of("PL"));

		mockMvc.perform(post("/api/coupons/" + limitedCoupon.toLowerCase() + "/redemptions")
				.header("X-Forwarded-For", POLISH_CLIENT_IP)
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("userId", "empik-limit-user-1"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentRedemptions").value(1));

		mockMvc.perform(post("/api/coupons/" + limitedCoupon.toLowerCase() + "/redemptions")
				.header("X-Forwarded-For", POLISH_CLIENT_IP)
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("userId", "empik-limit-user-2"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Coupon '" + limitedCoupon + "' has reached its redemption limit"));
	}

	@Test
	void shouldRejectRedemptionFromDifferentCountry() throws Exception {
		String polishOnlyCoupon = "EMPIKPLONLY";
		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", polishOnlyCoupon, "maxRedemptions", 2, "countryCode", "PL"))))
				.andExpect(status().isCreated());

		when(geoIpService.resolveCountry(FOREIGN_CLIENT_IP)).thenReturn(CountryCode.of("DE"));

		mockMvc.perform(post("/api/coupons/" + polishOnlyCoupon.toLowerCase() + "/redemptions")
				.header("X-Forwarded-For", FOREIGN_CLIENT_IP)
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("userId", "empik-abroad-user"))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(
						"Coupon '%s' is limited to country 'PL', but request came from 'DE'".formatted(polishOnlyCoupon)));
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}
}
