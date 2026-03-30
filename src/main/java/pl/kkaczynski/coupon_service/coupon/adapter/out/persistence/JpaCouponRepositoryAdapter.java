package pl.kkaczynski.coupon_service.coupon.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import pl.kkaczynski.coupon_service.coupon.domain.Coupon;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCode;
import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.out.CouponRepository;

@Repository
public class JpaCouponRepositoryAdapter implements CouponRepository {

	private final SpringDataCouponJpaRepository repository;

	public JpaCouponRepositoryAdapter(SpringDataCouponJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean existsByCode(CouponCode code) {
		return repository.existsByCodeNormalized(code.value());
	}

	@Override
	public Optional<Coupon> findByCode(CouponCode code) {
		return repository.findByCodeNormalized(code.value()).map(this::toDomain);
	}

	@Override
	public Optional<Coupon> findByCodeForUpdate(CouponCode code) {
		return repository.findByCodeNormalizedForUpdate(code.value()).map(this::toDomain);
	}

	@Override
	public Coupon save(Coupon coupon) {
		return toDomain(repository.save(toEntity(coupon)));
	}

	private Coupon toDomain(CouponJpaEntity entity) {
		return new Coupon(
				entity.getId(),
				CouponCode.of(entity.getCodeNormalized()),
				entity.getCreatedAt(),
				entity.getMaxRedemptions(),
				entity.getCurrentRedemptions(),
				CountryCode.of(entity.getCountryCode())
		);
	}

	private CouponJpaEntity toEntity(Coupon coupon) {
		return new CouponJpaEntity(
				coupon.id(),
				coupon.code().value(),
				coupon.createdAt(),
				coupon.maxRedemptions(),
				coupon.currentRedemptions(),
				coupon.countryCode().value()
		);
	}
}
