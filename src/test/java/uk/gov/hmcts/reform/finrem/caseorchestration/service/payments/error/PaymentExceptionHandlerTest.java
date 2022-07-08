package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentDuplicateError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseClient401Error;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseClient404Error;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseClient422Error;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseErrorToString;


@RunWith(MockitoJUnitRunner.class)
public class PaymentExceptionHandlerTest {

    @Spy
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private PaymentExceptionHandler exceptionHandler;

    @Test
    public void handleDuplicatePaymentError() {
        ResponseEntity<Object> response = exceptionHandler.handlePaymentException(duplicatePaymentError());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(paymentDuplicateError()));
    }

    @Test
    public void handleClient422Exception() {
        ResponseEntity<Object> response = exceptionHandler.handlePaymentException(paymentApi422Error());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(paymentResponseClient422Error()));
    }

    @Test
    public void handleClient404Exception() {
        ResponseEntity<Object> response = exceptionHandler.handlePaymentException(paymentApi404Error());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(paymentResponseClient404Error()));
    }

    @Test
    public void handleClient401Exception() {
        ResponseEntity<Object> response = exceptionHandler.handlePaymentException(paymentApi401Error());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(paymentResponseClient401Error()));
    }

    @Test
    public void handleClient403ValidationException() {
        ResponseEntity<Object> response = exceptionHandler.handlePaymentException(paymentApi403ValidationError());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(paymentResponseClient401Error()));
    }

    @Test
    public void handleServerException() {
        ResponseEntity<Object> response = exceptionHandler.handlePaymentException(paymentApiServerError());

        assertThat(response.getStatusCode(), is(INTERNAL_SERVER_ERROR));
    }


    private PaymentException paymentApiServerError() {
        return new PaymentException(new HttpServerErrorException(SERVICE_UNAVAILABLE));
    }

    private PaymentException duplicatePaymentError() {
        return new PaymentException(
                new HttpClientErrorException(BAD_REQUEST, "",
                        "duplicate payment".getBytes(), UTF_8)
        );

    }

    private PaymentException paymentApi422Error() {
        return new PaymentException(
                new HttpClientErrorException(UNPROCESSABLE_ENTITY, "",
                        "Invalid or missing attribute".getBytes(), UTF_8)
        );
    }

    private PaymentException paymentApi404Error() {
        return new PaymentException(
                new HttpClientErrorException(NOT_FOUND, "",
                        "Account information could not be found".getBytes(), UTF_8)
        );
    }

    private PaymentException paymentApi401Error() {
        return new PaymentException(
                new HttpClientErrorException(FORBIDDEN, "",
                        paymentResponseErrorToString().getBytes(), UTF_8)
        );
    }

    private PaymentException paymentApi403ValidationError() {
        return new PaymentException(
                new HttpClientErrorException(FORBIDDEN, "",
                        paymentResponseErrorToString().getBytes(), UTF_8)
        );
    }
}