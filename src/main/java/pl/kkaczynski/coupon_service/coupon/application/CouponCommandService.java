package pl.kkaczynski.coupon_service.coupon.application;

import java.time.Clock;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.kkaczynski.coupon_service.coupon.domain.Coupon;
import pl.kkaczynski.coupon_service.coupon.domain.CouponAlreadyExistsException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCode;
import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.in.CreateCouponUseCase;
import pl.kkaczynski.coupon_service.coupon.port.in.RedeemCouponUseCase;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRepository;
import pl.kkaczynski.coupon_service.coupon.port.out.GeoIpService;

@Service
public class CouponCommandService implements CreateCouponUseCase, RedeemCouponUseCase {

	private final CouponRepository couponRepository;
	private final GeoIpService geoIpService;
	private final CouponRedemptionService couponRedemptionService;
	private final Clock clock;

	public CouponCommandService(
			CouponRepository couponRepository,
			GeoIpService geoIpService,
			CouponRedemptionService couponRedemptionService,
			Clock clock
	) {
		this.couponRepository = couponRepository;
		this.geoIpService = geoIpService;
		this.couponRedemptionService = couponRedemptionService;
		this.clock = clock;
	}

	@Override
	@Transactional
	public CouponDetails createCoupon(CreateCouponCommand command) {
		CouponCode code = CouponCode.of(command.code());
		CountryCode countryCode = CountryCode.of(command.countryCode());

		if (couponRepository.existsByCode(code)) {
			throw new CouponAlreadyExistsException(code.value());
		}

		Coupon coupon = Coupon.create(code, clock.instant(), command.maxRedemptions(), countryCode);

		try {
			Coupon savedCoupon = couponRepository.save(coupon);
			return toCouponDetails(savedCoupon);
		} catch (DataIntegrityViolationException exception) {
			throw new CouponAlreadyExistsException(code.value());
		}
	}

	@Override
	public CouponRedemptionDetails redeemCoupon(RedeemCouponCommand command) {
		CountryCode requesterCountry = geoIpService.resolveCountry(command.clientIp());
		return couponRedemptionService.redeem(command.code(), command.userId(), requesterCountry);
	}

	private CouponDetails toCouponDetails(Coupon coupon) {
		return new CouponDetails(
				coupon.code().value(),
				coupon.createdAt(),
				coupon.maxRedemptions(),
				coupon.currentRedemptions(),
				coupon.countryCode().value()
		);
	}
}
