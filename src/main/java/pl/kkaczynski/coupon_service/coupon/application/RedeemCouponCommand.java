package pl.kkaczynski.coupon_service.coupon.application;

public record RedeemCouponCommand(String code, String userId, String clientIp) {
}
