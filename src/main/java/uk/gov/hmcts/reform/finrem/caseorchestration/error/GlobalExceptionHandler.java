package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String SERVER_ERROR_MSG = "Some server side exception occurred. Please check logs for details";

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleFeignException(FeignException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.status()).body(SERVER_ERROR_MSG);
    }

    @ExceptionHandler(InvalidCaseDataException.class)
    ResponseEntity<Object> handleInvalidCaseDataException(InvalidCaseDataException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.status()).body(SERVER_ERROR_MSG);
    }

    @ExceptionHandler(UnsuccessfulDocumentGenerateException.class)
    ResponseEntity<Object> handleDocumentGenerationException(UnsuccessfulDocumentGenerateException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SERVER_ERROR_MSG);
    }
}
