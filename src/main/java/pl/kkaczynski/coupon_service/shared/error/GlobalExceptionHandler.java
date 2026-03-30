package pl.kkaczynski.coupon_service.shared.error;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pl.kkaczynski.coupon_service.coupon.domain.CouponAlreadyExistsException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponAlreadyRedeemedException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponCountryMismatchException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponLimitReachedException;
import pl.kkaczynski.coupon_service.coupon.domain.CouponNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final Clock clock;

	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(CouponNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleCouponNotFound(CouponNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
	}

	@ExceptionHandler({
			CouponAlreadyExistsException.class,
			CouponAlreadyRedeemedException.class,
			CouponLimitReachedException.class
	})
	ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
		return buildError(HttpStatus.CONFLICT, exception.getMessage(), request, null);
	}

	@ExceptionHandler(CouponCountryMismatchException.class)
	ResponseEntity<ApiErrorResponse> handleCountryMismatch(CouponCountryMismatchException exception, HttpServletRequest request) {
		return buildError(HttpStatus.FORBIDDEN, exception.getMessage(), request, null);
	}

	@ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
	ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> details = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			details.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return buildError(HttpStatus.BAD_REQUEST, "Request validation failed", request, details);
	}

	@ExceptionHandler(GeoIpLookupException.class)
	ResponseEntity<ApiErrorResponse> handleGeoIpLookup(GeoIpLookupException exception, HttpServletRequest request) {
		return buildError(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, null);
	}

	private ResponseEntity<ApiErrorResponse> buildError(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			Map<String, String> details
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(clock),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				details
		);
		return ResponseEntity.status(status).body(response);
	}
}
