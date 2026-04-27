package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationContestedAboutToStartHandlerTest {

    @InjectMocks
    private AmendApplicationContestedAboutToStartHandler handler;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Mock
    private MiamLegacyExemptionsService miamLegacyExemptionsService;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS);
    }

    @Test
    void givenCaseWithValidLegacyMiamExemptions_whenHandled_thenPrepopulateFieldsAndNoWarnings() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        when(miamLegacyExemptionsService.isLegacyExemptionsInvalid(any(MiamWrapper.class))).thenReturn(false);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(onStartDefaultValueService).defaultCivilPartnershipField(callbackRequest),
            () -> verify(onStartDefaultValueService).defaultTypeOfApplication(callbackRequest),
            () -> verify(miamLegacyExemptionsService).isLegacyExemptionsInvalid(any(MiamWrapper.class)),
            () -> assertFalse(response.hasWarnings())
        );
    }

    @Test
    void givenCaseWithInvalidLegacyMiamExemptions_whenHandle_thenReturnsWarningsAndLegacyExemptionsConverted() {
        MiamWrapper miamWrapper = mock(MiamWrapper.class);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .miamWrapper(miamWrapper)
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        when(miamLegacyExemptionsService.isLegacyExemptionsInvalid(miamWrapper)).thenReturn(true);
        List<String> invalidLegacyExemptions = List.of("MOCKED WARNINGS");
        when(miamLegacyExemptionsService.getInvalidLegacyExemptions(miamWrapper)).thenReturn(invalidLegacyExemptions);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getWarnings()).contains(
                "MOCKED WARNINGS",
                "The following MIAM exemptions are no longer valid and will be removed from the case data."
            ),
            () -> verify(miamLegacyExemptionsService).isLegacyExemptionsInvalid(miamWrapper),
            () -> verify(miamLegacyExemptionsService).getInvalidLegacyExemptions(miamWrapper),
            () -> verify(miamLegacyExemptionsService).convertLegacyExemptions(miamWrapper),
            () -> verifyNoMoreInteractions(miamLegacyExemptionsService)
        );
    }

    @Test
    void testPopulateInRefugeQuestionsCalled() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateRespondentInRefugeTab is called with our case details instance
            assertAll(
                () -> mockedStatic.verify(() -> RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails)),
                () -> mockedStatic.verify(() -> RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails))
            );
        }
    }

    @Test
    void testCurrentUserCaseRoleTypeCorrectlySet() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest callbackRequest =  FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            finremCaseData);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        when(assignCaseAccessService.getActiveUser(caseDetails.getId().toString(),
            AUTH_TOKEN)).thenReturn("an appropriate value");

        handler.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
            () -> assertEquals("an appropriate value", finremCaseData.getCurrentUserCaseRoleType()),
            () -> verify(assignCaseAccessService).getActiveUser(CASE_ID, AUTH_TOKEN),
            () -> verifyNoMoreInteractions(assignCaseAccessService)
        );
    }
}
