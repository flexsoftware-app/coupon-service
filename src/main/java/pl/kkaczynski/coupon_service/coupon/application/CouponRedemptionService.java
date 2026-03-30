package pl.kkaczynski.coupon_service.coupon.application;

import java.time.Clock;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.kkaczynski.coupon_service.coupon.domain.CouponAlreadyRedeemedException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCode;
import pl.kkaczynski.coupon_service.coupon.domain.CouponNotFoundException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponRedemption;
import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRedemptionRepository;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRepository;

@Service
public class CouponRedemptionService {

	private final CouponRepository couponRepository;
	private final CouponRedemptionRepository couponRedemptionRepository;
	private final Clock clock;

	public CouponRedemptionService(
			CouponRepository couponRepository,
			CouponRedemptionRepository couponRedemptionRepository,
			Clock clock
	) {
		this.couponRepository = couponRepository;
		this.couponRedemptionRepository = couponRedemptionRepository;
		this.clock = clock;
	}

	@Transactional
	public CouponRedemptionDetails redeem(String rawCode, String userId, CountryCode requesterCountry) {
		CouponCode code = CouponCode.of(rawCode);
		var coupon = couponRepository.findByCodeForUpdate(code)
				.orElseThrow(() -> new CouponNotFoundException(code.value()));

		if (couponRedemptionRepository.existsByCouponIdAndUserId(coupon.id(), userId)) {
			throw new CouponAlreadyRedeemedException(code.value(), userId);
		}

		var updatedCoupon = coupon.redeem(requesterCountry);
		var redemption = CouponRedemption.create(coupon.id(), userId, clock.instant());

		try {
			couponRedemptionRepository.save(redemption);
		} catch (DataIntegrityViolationException exception) {
			throw new CouponAlreadyRedeemedException(code.value(), userId);
		}
		couponRepository.save(updatedCoupon);

		return new CouponRedemptionDetails(
				updatedCoupon.code().value(),
				redemption.userId(),
				redemption.redeemedAt(),
				updatedCoupon.currentRedemptions(),
				updatedCoupon.maxRedemptions(),
				updatedCoupon.countryCode().value()
		);
	}
}
