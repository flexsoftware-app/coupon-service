package pl.kkaczynski.coupon_service.coupon.port.out;

import java.util.Optional;

import pl.kkaczynski.coupon_service.coupon.domain.Coupon;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCode;

public interface CouponRepository {

	boolean existsByCode(CouponCode code);

	Optional<Coupon> findByCode(CouponCode code);

	Optional<Coupon> findByCodeForUpdate(CouponCode code);

	Coupon save(Coupon coupon);
}
