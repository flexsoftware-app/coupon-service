package pl.kkaczynski.coupon_service.coupon.port.out;

import java.util.UUID;

import pl.kkaczynski.coupon_service.coupon.domain.CouponRedemption;

public interface CouponRedemptionRepository {

	boolean existsByCouponIdAndUserId(UUID couponId, String userId);

	CouponRedemption save(CouponRedemption redemption);
}
