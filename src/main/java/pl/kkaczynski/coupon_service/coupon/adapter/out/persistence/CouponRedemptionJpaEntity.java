package pl.kkaczynski.coupon_service.coupon.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "coupon_redemptions",
		indexes = @Index(name = "idx_coupon_redemptions_coupon_id", columnList = "coupon_id"),
		uniqueConstraints = @UniqueConstraint(name = "uk_coupon_redemptions_coupon_user", columnNames = {"coupon_id", "user_id"})
)
public class CouponRedemptionJpaEntity {

	@Id
	private UUID id;

	@Column(name = "coupon_id", nullable = false)
	private UUID couponId;

	@Column(name = "user_id", nullable = false, length = 100)
	private String userId;

	@Column(name = "redeemed_at", nullable = false)
	private Instant redeemedAt;

	protected CouponRedemptionJpaEntity() {
	}

	public CouponRedemptionJpaEntity(UUID id, UUID couponId, String userId, Instant redeemedAt) {
		this.id = id;
		this.couponId = couponId;
		this.userId = userId;
		this.redeemedAt = redeemedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getCouponId() {
		return couponId;
	}

	public String getUserId() {
		return userId;
	}

	public Instant getRedeemedAt() {
		return redeemedAt;
	}
}
