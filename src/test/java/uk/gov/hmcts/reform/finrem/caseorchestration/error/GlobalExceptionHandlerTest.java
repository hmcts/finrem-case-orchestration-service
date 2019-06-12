package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler.SERVER_ERROR_MSG;

public class GlobalExceptionHandlerTest {
    GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    public void handleFeignException() {
        ResponseEntity<Object> actual = exceptionHandler.handleFeignException(feignError());
        assertThat(actual.getStatusCodeValue(), is(INTERNAL_SERVER_ERROR));
        assertThat(actual.getBody(), is(SERVER_ERROR_MSG));
    }

    @Test
    public void handleInvalidCaseDataException() {
        ResponseEntity<Object> actual = exceptionHandler.handleInvalidCaseDataException(invalidCaseDataError());
        assertThat(actual.getStatusCodeValue(), is(BAD_REQUEST));
        assertThat(actual.getBody(), is(SERVER_ERROR_MSG));
    }

    @Test
    public void handleServerErrorException() {
        ResponseEntity<Object> actual = exceptionHandler.handleServerErrorException(httpServerError());
        assertThat(actual.getStatusCodeValue(), is(INTERNAL_SERVER_ERROR));
        assertThat(actual.getBody(), is(SERVER_ERROR_MSG));
    }
}