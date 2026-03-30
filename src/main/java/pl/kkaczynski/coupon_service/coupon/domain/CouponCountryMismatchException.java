package pl.kkaczynski.coupon_service.coupon.domain;

public class CouponCountryMismatchException extends RuntimeException {

	public CouponCountryMismatchException(String code, String expectedCountry, String actualCountry) {
		super("Coupon '%s' is limited to country '%s', but request came from '%s'".formatted(code, expectedCountry, actualCountry));
	}
}
