package pl.kkaczynski.coupon_service.coupon.adapter.in.web;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {

	private static final Set<String> LOOPBACK_ADDRESSES = Set.of("127.0.0.1", "::1", "0:0:0:0:0:0:0:1");

	private final String trustedProxiesProperty;
	private Set<String> trustedProxies;

	public ClientIpResolver(@Value("${security.trusted-proxies:127.0.0.1,::1}") String trustedProxiesProperty) {
		this.trustedProxiesProperty = trustedProxiesProperty;
	}

	@PostConstruct
	void initTrustedProxies() {
		this.trustedProxies = Arrays.stream(trustedProxiesProperty.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.collect(Collectors.toSet());
	}

	public String resolve(HttpServletRequest request) {
		String remoteAddress = normalize(request.getRemoteAddr());
		if (isTrustedProxy(remoteAddress)) {
			String forwardedFor = request.getHeader("X-Forwarded-For");
			if (StringUtils.hasText(forwardedFor)) {
				return extractFirstForwardedIp(forwardedFor);
			}

			String realIp = request.getHeader("X-Real-IP");
			if (StringUtils.hasText(realIp)) {
				return normalize(realIp);
			}
		}

		if (StringUtils.hasText(remoteAddress)) {
			return remoteAddress;
		}

		throw new IllegalArgumentException("Client IP address is missing");
	}

	private boolean isTrustedProxy(String remoteAddress) {
		return LOOPBACK_ADDRESSES.contains(remoteAddress) || trustedProxies.contains(remoteAddress);
	}

	private String extractFirstForwardedIp(String forwardedFor) {
		String firstAddress = forwardedFor.split(",")[0];
		return normalize(firstAddress);
	}

	private String normalize(String ip) {
		if (!StringUtils.hasText(ip)) {
			return "";
		}
		String normalized = ip.trim();
		if (normalized.startsWith("[") && normalized.endsWith("]") && normalized.length() > 2) {
			return normalized.substring(1, normalized.length() - 1);
		}
		return normalized;
	}
}
