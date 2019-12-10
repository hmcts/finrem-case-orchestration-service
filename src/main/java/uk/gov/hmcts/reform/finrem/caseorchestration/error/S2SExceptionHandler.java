package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.bsp.common.error.ForbiddenException;
import uk.gov.hmcts.reform.bsp.common.error.UnauthenticatedException;

@ControllerAdvice
@Slf4j
public class S2SExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    ResponseEntity<Object> handleUnauthenticatedException(UnauthenticatedException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<Object> handleForbiddenException(ForbiddenException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }
}
