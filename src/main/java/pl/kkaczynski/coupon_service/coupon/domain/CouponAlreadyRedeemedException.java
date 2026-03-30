package pl.kkaczynski.coupon_service.coupon.domain;

public class CouponAlreadyRedeemedException extends RuntimeException {

	public CouponAlreadyRedeemedException(String code, String userId) {
		super("User '%s' has already redeemed coupon '%s'".formatted(userId, code));
	}
}
