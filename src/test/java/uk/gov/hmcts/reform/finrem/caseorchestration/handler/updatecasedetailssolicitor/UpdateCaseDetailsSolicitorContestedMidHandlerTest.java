package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorContestedMidHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsSolicitorContestedMidHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, MID_EVENT, CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR);
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
            Arguments.of(
                List.of("address error 1"),
                List.of("email error 1"),
                List.of("address error 1", "email error 1")
            ),
            Arguments.of(
                List.of(),
                List.of(),
                List.of()
            ),
            Arguments.of(
                List.of("address only"),
                List.of(),
                List.of("address only")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    void testHandle(List<String> addressErrors,
                    List<String> emailErrors,
                    List<String> expectedErrors) {

        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.create(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData);

        try (MockedStatic<ContactDetailsValidator> contactValidatorMock = mockStatic(ContactDetailsValidator.class)) {
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataAddresses(caseData))
                .thenReturn(new ArrayList<>(addressErrors));
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataEmailAddresses(caseData))
                .thenReturn(new ArrayList<>(emailErrors));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);
        }
    }
}
