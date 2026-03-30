package pl.kkaczynski.coupon_service.coupon.adapter.out.geoip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import pl.kkaczynski.coupon_service.shared.error.GeoIpLookupException;

class IpWhoIsGeoIpClientTest {

	private MockRestServiceServer server;
	private IpWhoIsGeoIpClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.baseUrl("https://ipwho.is").build();
		client = new IpWhoIsGeoIpClient(restClient);
	}

	@Test
	void shouldResolveCountryWhenProviderReturnsSuccess() {
		server.expect(requestTo("https://ipwho.is/1.2.3.4?fields=success,country_code"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"success\":true,\"country_code\":\"PL\"}", MediaType.APPLICATION_JSON));

		assertEquals("PL", client.resolveCountry("1.2.3.4").value());
		server.verify();
	}

	@Test
	void shouldThrowGeoIpLookupExceptionWhenProviderReturnsFailurePayload() {
		server.expect(requestTo("https://ipwho.is/1.2.3.4?fields=success,country_code"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"success\":false}", MediaType.APPLICATION_JSON));

		assertThrows(GeoIpLookupException.class, () -> client.resolveCountry("1.2.3.4"));
		server.verify();
	}

	@Test
	void shouldThrowGeoIpLookupExceptionWhenProviderResponseHasNoCountryCode() {
		server.expect(requestTo("https://ipwho.is/1.2.3.4?fields=success,country_code"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"success\":true,\"country_code\":\"\"}", MediaType.APPLICATION_JSON));

		assertThrows(GeoIpLookupException.class, () -> client.resolveCountry("1.2.3.4"));
		server.verify();
	}
}
