package pl.kkaczynski.coupon_service.coupon.application;

public record CreateCouponCommand(String code, int maxRedemptions, String countryCode) {
}
