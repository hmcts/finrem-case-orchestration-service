package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleFeignException(FeignException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.status(exception.status()).body(exception.getMessage());
    }

    @ExceptionHandler(InvalidCaseDataException.class)
    ResponseEntity<Object> handleInvalidCaseDataException(InvalidCaseDataException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.status(exception.status()).body(exception.getMessage());
    }
}
