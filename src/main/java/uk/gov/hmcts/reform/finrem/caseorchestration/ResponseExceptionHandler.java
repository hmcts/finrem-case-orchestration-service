package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.ForbiddenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnauthenticatedException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApiError;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ResponseExceptionHandler.class);
    private static final ApiError API_ERROR_FORBIDDEN = new ApiError("S2S token is not authorized to use the service");

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<ApiError> handleInvalidTokenException(InvalidTokenException exc) {
        log.warn(exc.getMessage(), exc);
        return status(UNAUTHORIZED).body(new ApiError(exc.getMessage()));
    }

    @ExceptionHandler(UnauthenticatedException.class)
    protected ResponseEntity<ApiError> handleUnauthenticatedException(UnauthenticatedException exc) {
        log.warn(exc.getMessage(), exc);
        return status(UNAUTHORIZED).body(new ApiError(exc.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<ApiError> handleForbiddenException(ForbiddenException exc) {
        log.warn(exc.getMessage(), exc);
        return status(FORBIDDEN).body(API_ERROR_FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Void> handleInternalException(Exception exc) {
        log.error(exc.getMessage(), exc);
        return status(INTERNAL_SERVER_ERROR).build();
    }
}
