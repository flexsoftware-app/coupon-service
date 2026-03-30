package pl.kkaczynski.coupon_service.coupon.application;

import java.time.Instant;

public record CouponDetails(
		String code,
		Instant createdAt,
		int maxRedemptions,
		int currentRedemptions,
		String countryCode
) {
}
