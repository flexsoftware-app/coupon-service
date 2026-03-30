package pl.kkaczynski.coupon_service.coupon.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCouponJpaRepository extends JpaRepository<CouponJpaEntity, UUID> {

	boolean existsByCodeNormalized(String codeNormalized);

	Optional<CouponJpaEntity> findByCodeNormalized(String codeNormalized);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from CouponJpaEntity c where c.codeNormalized = :codeNormalized")
	Optional<CouponJpaEntity> findByCodeNormalizedForUpdate(@Param("codeNormalized") String codeNormalized);
}
