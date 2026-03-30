package pl.kkaczynski.coupon_service.coupon.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCouponRequest(
		@NotBlank(message = "code is required")
		String code,
		@Min(value = 1, message = "maxRedemptions must be greater than zero")
		int maxRedemptions,
		@NotBlank(message = "countryCode is required")
		@Pattern(regexp = "(?i)[a-z]{2}", message = "countryCode must be a 2-letter ISO code")
		String countryCode
) {
}
