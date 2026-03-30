package pl.kkaczynski.coupon_service.coupon.port.in;

import pl.kkaczynski.coupon_service.coupon.application.CouponDetails;
import pl.kkaczynski.coupon_service.coupon.application.CreateCouponCommand;

public interface CreateCouponUseCase {

	CouponDetails createCoupon(CreateCouponCommand command);
}
