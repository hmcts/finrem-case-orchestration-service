package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.out.BspErrorResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApiError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;

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
        return ResponseEntity.status(exception.status()).body(SERVER_ERROR_MSG + ". Error: " + exception.getMessage());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    ResponseEntity<Object> handleServerErrorException(HttpServerErrorException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoSuchFieldExistsException.class)
    ResponseEntity<Object> handleNoSuchFieldExistsException(NoSuchFieldExistsException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SERVER_ERROR_MSG);
    }

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<ApiError> handleInvalidTokenException(InvalidTokenException exc) {
        log.warn(exc.getMessage(), exc);
        return status(UNAUTHORIZED).body(new ApiError(exc.getMessage()));
    }

    @ExceptionHandler(InvalidDataException.class)
    ResponseEntity<Object> handleInvalidDataException(InvalidDataException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                BspErrorResponse.builder()
                    .errors(mergeLists(exception.getWarnings(), exception.getErrors()))
                    .build()
            );
    }

    private List<String> mergeLists(List<String> warn, List<String> errors) {
        warn = Optional.ofNullable(warn).orElse(new ArrayList<>());
        warn.addAll(errors);

        return warn;
    }
}
