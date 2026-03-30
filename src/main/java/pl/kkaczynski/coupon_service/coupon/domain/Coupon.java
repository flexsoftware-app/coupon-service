package pl.kkaczynski.coupon_service.coupon.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Coupon(
		UUID id,
		CouponCode code,
		Instant createdAt,
		int maxRedemptions,
		int currentRedemptions,
		CountryCode countryCode
) {

	public Coupon {
		Objects.requireNonNull(id, "Coupon id is required");
		Objects.requireNonNull(code, "Coupon code is required");
		Objects.requireNonNull(createdAt, "Coupon creation time is required");
		Objects.requireNonNull(countryCode, "Coupon country is required");
		if (maxRedemptions < 1) {
			throw new IllegalArgumentException("Coupon max redemptions must be greater than zero");
		}
		if (currentRedemptions < 0) {
			throw new IllegalArgumentException("Coupon current redemptions must not be negative");
		}
		if (currentRedemptions > maxRedemptions) {
			throw new IllegalArgumentException("Coupon current redemptions must not exceed max redemptions");
		}
	}

	public static Coupon create(CouponCode code, Instant createdAt, int maxRedemptions, CountryCode countryCode) {
		return new Coupon(UUID.randomUUID(), code, createdAt, maxRedemptions, 0, countryCode);
	}

	public Coupon redeem(CountryCode requesterCountry) {
		if (!countryCode.equals(requesterCountry)) {
			throw new CouponCountryMismatchException(code.value(), countryCode.value(), requesterCountry.value());
		}
		if (currentRedemptions >= maxRedemptions) {
			throw new CouponLimitReachedException(code.value());
		}
		return new Coupon(id, code, createdAt, maxRedemptions, currentRedemptions + 1, countryCode);
	}
}
