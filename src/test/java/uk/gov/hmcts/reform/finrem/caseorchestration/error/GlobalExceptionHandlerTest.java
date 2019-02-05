package uk.gov.hmcts.reform.finrem.caseorchestration.error;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.STATUS_CODE;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    public void handleFeignException() {
        ResponseEntity<Object> actual = exceptionHandler.handleFeignException(feignError());
        assertThat(actual.getStatusCodeValue(), Matchers.is(STATUS_CODE));
    }
}