package pl.kkaczynski.coupon_service.coupon.application;

import java.time.Instant;

public record CouponRedemptionDetails(
		String code,
		String userId,
		Instant redeemedAt,
		int currentRedemptions,
		int maxRedemptions,
		String countryCode
) {
}
