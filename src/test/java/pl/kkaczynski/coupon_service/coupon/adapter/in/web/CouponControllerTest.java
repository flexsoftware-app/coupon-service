package pl.kkaczynski.coupon_service.coupon.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import pl.kkaczynski.coupon_service.coupon.application.CouponDetails;
import pl.kkaczynski.coupon_service.coupon.application.CouponRedemptionDetails;
import pl.kkaczynski.coupon_service.coupon.port.in.CreateCouponUseCase;
import pl.kkaczynski.coupon_service.coupon.port.in.RedeemCouponUseCase;
import pl.kkaczynski.coupon_service.shared.config.TimeConfiguration;
import pl.kkaczynski.coupon_service.shared.error.GlobalExceptionHandler;

@WebMvcTest(CouponController.class)
@Import({GlobalExceptionHandler.class, TimeConfiguration.class, ClientIpResolver.class})
class CouponControllerTest {

	private static final String EMPIK_COUPON = "EMPIKBONUS";
	private static final String EMPIK_USER = "empik-reader-7";
	private static final String CLIENT_IP = "1.2.3.4";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CreateCouponUseCase createCouponUseCase;

	@MockBean
	private RedeemCouponUseCase redeemCouponUseCase;

	@Test
	void shouldReturnCreatedCoupon() throws Exception {
		when(createCouponUseCase.createCoupon(any())).thenReturn(new CouponDetails(
				EMPIK_COUPON,
				Instant.parse("2025-01-01T10:00:00Z"),
				3,
				0,
				"PL"
		));

		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", EMPIK_COUPON.toLowerCase(), "maxRedemptions", 3, "countryCode", "pl"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value(EMPIK_COUPON))
				.andExpect(jsonPath("$.countryCode").value("PL"));
	}

	@Test
	void shouldReturnBadRequestForInvalidCreatePayload() throws Exception {
		mockMvc.perform(post("/api/coupons")
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("code", "", "maxRedemptions", 0, "countryCode", "POL"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Request validation failed"));
	}

	@Test
	void shouldReturnRedeemedCoupon() throws Exception {
		when(redeemCouponUseCase.redeemCoupon(any())).thenReturn(new CouponRedemptionDetails(
				EMPIK_COUPON,
				EMPIK_USER,
				Instant.parse("2025-01-01T10:05:00Z"),
				1,
				3,
				"PL"
		));

		mockMvc.perform(post("/api/coupons/" + EMPIK_COUPON.toLowerCase() + "/redemptions")
				.header("X-Forwarded-For", CLIENT_IP)
				.contentType(APPLICATION_JSON)
				.content(json(Map.of("userId", EMPIK_USER))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(EMPIK_USER))
				.andExpect(jsonPath("$.currentRedemptions").value(1));
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}
}
