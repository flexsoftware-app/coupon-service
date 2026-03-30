package pl.kkaczynski.coupon_service.coupon.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import pl.kkaczynski.coupon_service.coupon.domain.CouponRedemption;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRedemptionRepository;

@Repository
public class JpaCouponRedemptionRepositoryAdapter implements CouponRedemptionRepository {

	private final SpringDataCouponRedemptionJpaRepository repository;

	public JpaCouponRedemptionRepositoryAdapter(SpringDataCouponRedemptionJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean existsByCouponIdAndUserId(UUID couponId, String userId) {
		return repository.existsByCouponIdAndUserId(couponId, userId.trim());
	}

	@Override
	public CouponRedemption save(CouponRedemption redemption) {
		CouponRedemptionJpaEntity entity = new CouponRedemptionJpaEntity(
				redemption.id(),
				redemption.couponId(),
				redemption.userId(),
				redemption.redeemedAt()
		);
		CouponRedemptionJpaEntity savedEntity = repository.save(entity);
		return new CouponRedemption(
				savedEntity.getId(),
				savedEntity.getCouponId(),
				savedEntity.getUserId(),
				savedEntity.getRedeemedAt()
		);
	}
}
