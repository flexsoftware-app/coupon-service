package pl.kkaczynski.coupon_service.coupon.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record RedeemCouponRequest(
		@NotBlank(message = "userId is required")
		String userId
) {
}
