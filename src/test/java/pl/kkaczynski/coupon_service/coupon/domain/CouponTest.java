package pl.kkaczynski.coupon_service.coupon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CouponTest {

	@Test
	void shouldIncreaseRedemptionCountForMatchingCountry() {
		Coupon coupon = new Coupon(
				UUID.randomUUID(),
				CouponCode.of("wiosna"),
				Instant.parse("2025-01-01T10:00:00Z"),
				2,
				0,
				CountryCode.of("PL")
		);

		Coupon redeemedCoupon = coupon.redeem(CountryCode.of("PL"));

		assertEquals(1, redeemedCoupon.currentRedemptions());
	}

	@Test
	void shouldRejectRedemptionFromDifferentCountry() {
		Coupon coupon = new Coupon(
				UUID.randomUUID(),
				CouponCode.of("wiosna"),
				Instant.parse("2025-01-01T10:00:00Z"),
				2,
				0,
				CountryCode.of("PL")
		);

		assertThrows(CouponCountryMismatchException.class, () -> coupon.redeem(CountryCode.of("DE")));
	}

	@Test
	void shouldRejectRedemptionWhenLimitReached() {
		Coupon coupon = new Coupon(
				UUID.randomUUID(),
				CouponCode.of("wiosna"),
				Instant.parse("2025-01-01T10:00:00Z"),
				1,
				1,
				CountryCode.of("PL")
		);

		assertThrows(CouponLimitReachedException.class, () -> coupon.redeem(CountryCode.of("PL")));
	}
}
