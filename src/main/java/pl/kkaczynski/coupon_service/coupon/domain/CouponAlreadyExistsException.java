package pl.kkaczynski.coupon_service.coupon.domain;

public class CouponAlreadyExistsException extends RuntimeException {

	public CouponAlreadyExistsException(String code) {
		super("Coupon with code '%s' already exists".formatted(code));
	}
}
