package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnauthenticatedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private static final String SERVICE_HEADER = "some-header";
    private static final String TEST_SERVICE = "some-service";
    private static final String MISSING_SERVICEAUTH_HEADER_ERROR = "Missing ServiceAuthorization header";

    @Mock
    private AuthTokenValidator authTokenValidator;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService(authTokenValidator);
    }

    @AfterEach
    public void tearDown() {
        reset(authTokenValidator);
    }

    @Test
    public void should_throw_missing_header_exception_when_it_is_null() {
        Throwable exception = catchThrowable(() -> authService.authenticate(null));

        assertThat(exception)
                .isInstanceOf(UnauthenticatedException.class)
                .hasMessage(MISSING_SERVICEAUTH_HEADER_ERROR);

        verify(authTokenValidator, never()).getServiceName(anyString());
    }

    @Test
    public void should_track_failure_in_service_dependency_when_invalid_token_received() {
        willThrow(InvalidTokenException.class).given(authTokenValidator).getServiceName(anyString());

        Throwable exception = catchThrowable(() -> authService.authenticate(SERVICE_HEADER));

        assertThat(exception).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void should_track_successful_service_dependency_when_valid_token_received() {
        given(authTokenValidator.getServiceName(SERVICE_HEADER)).willReturn(TEST_SERVICE);

        String serviceName = authService.authenticate(SERVICE_HEADER);

        assertThat(serviceName).isEqualTo(TEST_SERVICE);
    }
}
