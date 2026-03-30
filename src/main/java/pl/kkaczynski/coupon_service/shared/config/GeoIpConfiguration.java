package pl.kkaczynski.coupon_service.shared.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeoIpConfiguration {

	@Bean
	RestClient ipWhoIsRestClient(RestClient.Builder restClientBuilder) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(2));

		return restClientBuilder
				.baseUrl("https://ipwho.is")
				.requestFactory(requestFactory)
				.build();
	}
}
