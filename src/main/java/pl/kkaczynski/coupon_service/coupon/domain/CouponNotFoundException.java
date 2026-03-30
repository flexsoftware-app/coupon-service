package pl.kkaczynski.coupon_service.coupon.domain;

public class CouponNotFoundException extends RuntimeException {

	public CouponNotFoundException(String code) {
		super("Coupon '%s' was not found".formatted(code));
	}
}
