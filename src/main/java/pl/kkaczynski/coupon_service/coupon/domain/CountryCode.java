package pl.kkaczynski.coupon_service.coupon.domain;

import java.util.Locale;
import java.util.Objects;

public record CountryCode(String value) {

	public CountryCode {
		Objects.requireNonNull(value, "Country code is required");
		if (!value.matches("[A-Z]{2}")) {
			throw new IllegalArgumentException("Country code must be a valid ISO-3166 alpha-2 code");
		}
	}

	public static CountryCode of(String rawValue) {
		Objects.requireNonNull(rawValue, "Country code is required");
		String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
		return new CountryCode(normalized);
	}
}
