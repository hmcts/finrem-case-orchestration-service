package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class PaymentExceptionHandler {

    private final ObjectMapper mapper;

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Object> handlePaymentException(PaymentException exception) {
        log.error("Exception occurred while making payment: {} ", exception.getMessage());

        if (exception.getCause() instanceof HttpClientErrorException) {
            HttpClientErrorException cause = (HttpClientErrorException) exception.getCause();
            HttpStatusCode errorCode = cause.getStatusCode();
            try {
                log.info("Payment error, exception: {} ", cause);
                if (errorCode == BAD_REQUEST || errorCode == NOT_FOUND || errorCode == UNPROCESSABLE_ENTITY) {
                    return ResponseEntity.ok(PaymentResponse.builder()
                            .error(errorCode.toString())
                            .message(cause.getResponseBodyAsString())
                            .build());
                }
                return ResponseEntity.ok(mapper.readValue(cause.getResponseBodyAsString(), PaymentResponse.class));
            } catch (IOException e) {
                log.error("payment-error-conversion exception : {} ", e);
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(GlobalExceptionHandler.SERVER_ERROR_MSG);
            }
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(GlobalExceptionHandler.SERVER_ERROR_MSG);
    }
}
