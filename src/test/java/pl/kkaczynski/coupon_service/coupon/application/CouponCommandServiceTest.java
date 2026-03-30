package pl.kkaczynski.coupon_service.coupon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kkaczynski.coupon_service.coupon.domain.Coupon;
import pl.kkaczynski.coupon_service.coupon.domain.CouponAlreadyExistsException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCode;
import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRepository;
import pl.kkaczynski.coupon_service.coupon.port.out.GeoIpService;

@ExtendWith(MockitoExtension.class)
class CouponCommandServiceTest {

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private GeoIpService geoIpService;

	@Mock
	private CouponRedemptionService couponRedemptionService;

	private CouponCommandService couponCommandService;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
		couponCommandService = new CouponCommandService(couponRepository, geoIpService, couponRedemptionService, clock);
	}

	@Test
	void shouldCreateCouponWithNormalizedValues() {
		when(couponRepository.existsByCode(CouponCode.of("wiosna"))).thenReturn(false);
		when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CouponDetails details = couponCommandService.createCoupon(new CreateCouponCommand("wiosna", 3, "pl"));

		assertEquals("WIOSNA", details.code());
		assertEquals(3, details.maxRedemptions());
		assertEquals(0, details.currentRedemptions());
		assertEquals("PL", details.countryCode());
	}

	@Test
	void shouldRejectDuplicateCoupon() {
		when(couponRepository.existsByCode(CouponCode.of("wiosna"))).thenReturn(true);

		assertThrows(
				CouponAlreadyExistsException.class,
				() -> couponCommandService.createCoupon(new CreateCouponCommand("wiosna", 3, "pl"))
		);

		verify(couponRepository, never()).save(any(Coupon.class));
	}

	@Test
	void shouldResolveCountryAndDelegateRedemption() {
		CouponRedemptionDetails expected = new CouponRedemptionDetails(
				"WIOSNA",
				"user-1",
				Instant.parse("2025-01-01T10:05:00Z"),
				1,
				3,
				"PL"
		);
		when(geoIpService.resolveCountry("1.2.3.4")).thenReturn(CountryCode.of("PL"));
		when(couponRedemptionService.redeem("wiosna", "user-1", CountryCode.of("PL"))).thenReturn(expected);

		CouponRedemptionDetails actual = couponCommandService.redeemCoupon(
				new RedeemCouponCommand("wiosna", "user-1", "1.2.3.4")
		);

		assertEquals(expected, actual);
	}
}
