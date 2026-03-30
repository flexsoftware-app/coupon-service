package pl.kkaczynski.coupon_service.coupon.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CouponRedemption(
		UUID id,
		UUID couponId,
		String userId,
		Instant redeemedAt
) {

	public CouponRedemption(UUID id, UUID couponId, String userId, Instant redeemedAt) {
		Objects.requireNonNull(id, "Redemption id is required");
		Objects.requireNonNull(couponId, "Coupon id is required");
		Objects.requireNonNull(redeemedAt, "Redemption time is required");
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("User id must not be blank");
		}

		this.id = id;
		this.couponId = couponId;
		this.userId = userId.trim();
		this.redeemedAt = redeemedAt;
	}

	public static CouponRedemption create(UUID couponId, String userId, Instant redeemedAt) {
		return new CouponRedemption(UUID.randomUUID(), couponId, userId, redeemedAt);
	}
}
