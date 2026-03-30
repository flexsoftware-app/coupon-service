package pl.kkaczynski.coupon_service.coupon.port.out;

import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;

public interface GeoIpService {

	CountryCode resolveCountry(String ipAddress);
}
