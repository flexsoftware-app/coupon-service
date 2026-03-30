package pl.kkaczynski.coupon_service.coupon.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

	@Test
	void shouldUseForwardedHeadersWhenRequestComesFromTrustedProxy() {
		ClientIpResolver resolver = new ClientIpResolver("10.0.0.1");
		resolver.initTrustedProxies();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("10.0.0.1");
		request.addHeader("X-Forwarded-For", "1.2.3.4, 10.0.0.1");

		assertEquals("1.2.3.4", resolver.resolve(request));
	}

	@Test
	void shouldIgnoreForwardedHeadersWhenRequestDoesNotComeFromTrustedProxy() {
		ClientIpResolver resolver = new ClientIpResolver("10.0.0.1");
		resolver.initTrustedProxies();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("8.8.8.8");
		request.addHeader("X-Forwarded-For", "1.2.3.4");
		request.addHeader("X-Real-IP", "1.2.3.5");

		assertEquals("8.8.8.8", resolver.resolve(request));
	}

	@Test
	void shouldTreatLoopbackAsTrustedProxy() {
		ClientIpResolver resolver = new ClientIpResolver("");
		resolver.initTrustedProxies();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("127.0.0.1");
		request.addHeader("X-Real-IP", "9.9.9.9");

		assertEquals("9.9.9.9", resolver.resolve(request));
	}
}
