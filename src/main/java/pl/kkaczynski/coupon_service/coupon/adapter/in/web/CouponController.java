package pl.kkaczynski.coupon_service.coupon.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.kkaczynski.coupon_service.coupon.application.CreateCouponCommand;
import pl.kkaczynski.coupon_service.coupon.application.CouponDetails;
import pl.kkaczynski.coupon_service.coupon.application.CouponRedemptionDetails;
import pl.kkaczynski.coupon_service.coupon.application.RedeemCouponCommand;
import pl.kkaczynski.coupon_service.coupon.port.in.CreateCouponUseCase;
import pl.kkaczynski.coupon_service.coupon.port.in.RedeemCouponUseCase;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

	private final CreateCouponUseCase createCouponUseCase;
	private final RedeemCouponUseCase redeemCouponUseCase;
	private final ClientIpResolver clientIpResolver;

	public CouponController(
			CreateCouponUseCase createCouponUseCase,
			RedeemCouponUseCase redeemCouponUseCase,
			ClientIpResolver clientIpResolver
	) {
		this.createCouponUseCase = createCouponUseCase;
		this.redeemCouponUseCase = redeemCouponUseCase;
		this.clientIpResolver = clientIpResolver;
	}

	@PostMapping
	public ResponseEntity<CouponDetails> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
		CouponDetails response = createCouponUseCase.createCoupon(
				new CreateCouponCommand(request.code(), request.maxRedemptions(), request.countryCode())
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{code}/redemptions")
	public ResponseEntity<CouponRedemptionDetails> redeemCoupon(
			@PathVariable String code,
			@Valid @RequestBody RedeemCouponRequest request,
			HttpServletRequest httpServletRequest
	) {
		CouponRedemptionDetails response = redeemCouponUseCase.redeemCoupon(
				new RedeemCouponCommand(code, request.userId(), clientIpResolver.resolve(httpServletRequest))
		);
		return ResponseEntity.ok(response);
	}
}
