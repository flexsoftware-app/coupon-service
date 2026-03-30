package pl.kkaczynski.coupon_service.coupon.domain;

public class CouponLimitReachedException extends RuntimeException {

	public CouponLimitReachedException(String code) {
		super("Coupon '%s' has reached its redemption limit".formatted(code));
	}
}
