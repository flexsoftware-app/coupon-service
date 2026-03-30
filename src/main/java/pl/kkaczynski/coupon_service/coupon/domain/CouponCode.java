package pl.kkaczynski.coupon_service.coupon.domain;

import java.util.Locale;
import java.util.Objects;

public record CouponCode(String value) {

	public CouponCode {
		Objects.requireNonNull(value, "Coupon code is required");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Coupon code must not be blank");
		}
	}

	public static CouponCode of(String rawValue) {
		Objects.requireNonNull(rawValue, "Coupon code is required");
		String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("Coupon code must not be blank");
		}
		return new CouponCode(normalized);
	}
}
