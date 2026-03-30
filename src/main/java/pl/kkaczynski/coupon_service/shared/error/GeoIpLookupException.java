package pl.kkaczynski.coupon_service.shared.error;

public class GeoIpLookupException extends RuntimeException {

	public GeoIpLookupException(String message) {
		super(message);
	}
}
