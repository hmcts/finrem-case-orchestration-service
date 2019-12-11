package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.httpServerError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.invalidCaseDataError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.noSuchFieldExistsCaseDataError;
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

    @Test
    public void handleNoSuchFieldExistsException() {
        ResponseEntity<Object> actual = exceptionHandler.handleNoSuchFieldExistsException(
                noSuchFieldExistsCaseDataError());
        assertThat(actual.getStatusCodeValue(), is(INTERNAL_SERVER_ERROR));
        assertThat(actual.getBody(), is(SERVER_ERROR_MSG));
    }
}