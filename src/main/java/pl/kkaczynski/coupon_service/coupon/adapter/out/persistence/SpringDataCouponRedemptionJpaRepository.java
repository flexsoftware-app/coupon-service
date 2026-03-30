package pl.kkaczynski.coupon_service.coupon.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCouponRedemptionJpaRepository extends JpaRepository<CouponRedemptionJpaEntity, UUID> {

	boolean existsByCouponIdAndUserId(UUID couponId, String userId);
}
