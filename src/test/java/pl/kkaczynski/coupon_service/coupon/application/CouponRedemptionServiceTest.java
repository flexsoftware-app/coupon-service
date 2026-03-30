package pl.kkaczynski.coupon_service.coupon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import pl.kkaczynski.coupon_service.coupon.domain.Coupon;
import pl.kkaczynski.coupon_service.coupon.domain.CouponAlreadyRedeemedException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCode;
import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRedemptionRepository;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRepository;

@ExtendWith(MockitoExtension.class)
class CouponRedemptionServiceTest {

	private static final String EMPIK_COUPON_CODE = "EMPIKBOOKS";
	private static final String EMPIK_USER_ID = "empik-reader-1";

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CouponRedemptionRepository couponRedemptionRepository;

	private CouponRedemptionService couponRedemptionService;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:05:00Z"), ZoneOffset.UTC);
		couponRedemptionService = new CouponRedemptionService(couponRepository, couponRedemptionRepository, clock);
	}

	@Test
	void shouldRedeemCouponAndPersistChanges() {
		UUID couponId = UUID.randomUUID();
		Coupon coupon = new Coupon(
				couponId,
				CouponCode.of(EMPIK_COUPON_CODE),
				Instant.parse("2025-01-01T10:00:00Z"),
				2,
				0,
				CountryCode.of("PL")
		);
		when(couponRepository.findByCodeForUpdate(CouponCode.of(EMPIK_COUPON_CODE))).thenReturn(Optional.of(coupon));
		when(couponRedemptionRepository.existsByCouponIdAndUserId(couponId, EMPIK_USER_ID)).thenReturn(false);
		when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(couponRedemptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		CouponRedemptionDetails result = couponRedemptionService.redeem(EMPIK_COUPON_CODE, EMPIK_USER_ID, CountryCode.of("PL"));

		assertEquals(EMPIK_COUPON_CODE, result.code());
		assertEquals(EMPIK_USER_ID, result.userId());
		assertEquals(1, result.currentRedemptions());
		verify(couponRedemptionRepository).save(any());
		verify(couponRepository).save(any(Coupon.class));
	}

	@Test
	void shouldRejectSecondRedemptionBySameUser() {
		UUID couponId = UUID.randomUUID();
		Coupon coupon = new Coupon(
				couponId,
				CouponCode.of(EMPIK_COUPON_CODE),
				Instant.parse("2025-01-01T10:00:00Z"),
				2,
				0,
				CountryCode.of("PL")
		);
		when(couponRepository.findByCodeForUpdate(CouponCode.of(EMPIK_COUPON_CODE))).thenReturn(Optional.of(coupon));
		when(couponRedemptionRepository.existsByCouponIdAndUserId(couponId, EMPIK_USER_ID)).thenReturn(true);

		assertThrows(
				CouponAlreadyRedeemedException.class,
				() -> couponRedemptionService.redeem(EMPIK_COUPON_CODE, EMPIK_USER_ID, CountryCode.of("PL"))
		);
	}

	@Test
	void shouldMapConcurrentDuplicateRedemptionToBusinessException() {
		UUID couponId = UUID.randomUUID();
		Coupon coupon = new Coupon(
				couponId,
				CouponCode.of(EMPIK_COUPON_CODE),
				Instant.parse("2025-01-01T10:00:00Z"),
				2,
				0,
				CountryCode.of("PL")
		);
		when(couponRepository.findByCodeForUpdate(CouponCode.of(EMPIK_COUPON_CODE))).thenReturn(Optional.of(coupon));
		when(couponRedemptionRepository.existsByCouponIdAndUserId(couponId, EMPIK_USER_ID)).thenReturn(false);
		when(couponRedemptionRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate redemption"));

		assertThrows(
				CouponAlreadyRedeemedException.class,
				() -> couponRedemptionService.redeem(EMPIK_COUPON_CODE, EMPIK_USER_ID, CountryCode.of("PL"))
		);
	}
}
