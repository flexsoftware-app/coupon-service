package pl.kkaczynski.coupon_service.coupon.adapter.out.geoip;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import pl.kkaczynski.coupon_service.coupon.domain.CountryCode;
import pl.kkaczynski.coupon_service.coupon.port.out.GeoIpService;
import pl.kkaczynski.coupon_service.shared.error.GeoIpLookupException;

@Component
public class IpWhoIsGeoIpClient implements GeoIpService {

	private final RestClient restClient;

	public IpWhoIsGeoIpClient(RestClient.Builder restClientBuilder) {
		this(buildRestClient(restClientBuilder));
	}

	IpWhoIsGeoIpClient(RestClient restClient) {
		this.restClient = restClient;
	}

	private static RestClient buildRestClient(RestClient.Builder restClientBuilder) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(2));

		return restClientBuilder
				.baseUrl("https://ipwho.is")
				.requestFactory(requestFactory)
				.build();
	}

	@Override
	public CountryCode resolveCountry(String ipAddress) {
		try {
			IpWhoIsResponse response = restClient.get()
					.uri(uriBuilder -> uriBuilder
							.pathSegment(ipAddress)
							.queryParam("fields", "success,country_code")
							.build())
					.retrieve()
					.body(IpWhoIsResponse.class);

			if (response == null || !response.success() || response.countryCode() == null || response.countryCode().isBlank()) {
				throw new GeoIpLookupException("Could not resolve country for IP address '%s'".formatted(ipAddress));
			}

			return CountryCode.of(response.countryCode());
		} catch (RestClientException exception) {
			throw new GeoIpLookupException("Could not resolve country for IP address '%s'".formatted(ipAddress));
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record IpWhoIsResponse(boolean success, @JsonProperty("country_code") String countryCode) {
	}
}
