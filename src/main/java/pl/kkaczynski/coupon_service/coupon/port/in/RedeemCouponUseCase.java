package pl.kkaczynski.coupon_service.coupon.port.in;

import pl.kkaczynski.coupon_service.coupon.application.CouponRedemptionDetails;
import pl.kkaczynski.coupon_service.coupon.application.RedeemCouponCommand;

public interface RedeemCouponUseCase {

	CouponRedemptionDetails redeemCoupon(RedeemCouponCommand command);
}
