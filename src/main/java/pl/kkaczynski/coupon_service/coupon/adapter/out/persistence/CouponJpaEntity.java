package pl.kkaczynski.coupon_service.coupon.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "coupons",
		uniqueConstraints = @UniqueConstraint(name = "uk_coupons_code_normalized", columnNames = "code_normalized")
)
public class CouponJpaEntity {

	@Id
	private UUID id;

	@Column(name = "code_normalized", nullable = false, length = 100)
	private String codeNormalized;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "max_redemptions", nullable = false)
	private int maxRedemptions;

	@Column(name = "current_redemptions", nullable = false)
	private int currentRedemptions;

	@Column(name = "country_code", nullable = false, length = 2)
	private String countryCode;

	protected CouponJpaEntity() {
	}

	public CouponJpaEntity(
			UUID id,
			String codeNormalized,
			Instant createdAt,
			int maxRedemptions,
			int currentRedemptions,
			String countryCode
	) {
		this.id = id;
		this.codeNormalized = codeNormalized;
		this.createdAt = createdAt;
		this.maxRedemptions = maxRedemptions;
		this.currentRedemptions = currentRedemptions;
		this.countryCode = countryCode;
	}

	public UUID getId() {
		return id;
	}

	public String getCodeNormalized() {
		return codeNormalized;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public int getMaxRedemptions() {
		return maxRedemptions;
	}

	public int getCurrentRedemptions() {
		return currentRedemptions;
	}

	public String getCountryCode() {
		return countryCode;
	}
}
