package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsMidHandlerTest {

    FinremCallbackRequest request;

    @Mock
    private ValidateHearingService validateHearingService;

    @Mock
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @InjectMocks
    private GeneralApplicationDirectionsMidHandler generalApplicationDirectionsMidHandler;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(generalApplicationDirectionsMidHandler, CallbackType.MID_EVENT, CaseType.CONTESTED,
            EventType.GENERAL_APPLICATION_DIRECTIONS_MH);
    }

    @Test
    void givenHearingRequiredWithWarningsAndInvalidAdditionalDocument_whenHandle_thenReturnsErrorsAndWarnings() {
        //Arrange
        WorkingHearing workingHearing = WorkingHearing.builder()
            .additionalHearingDocPrompt(YesOrNo.YES)
            .build();

        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .manageHearingsActionSelection(ManageHearingsAction.ADD_HEARING)
                .workingHearing(workingHearing)
                .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails())).thenReturn(true);
        when(validateHearingService.hasInvalidAdditionalHearingDocs(callbackRequest.getCaseDetails().getData()))
            .thenReturn(true);
        when(validateHearingService.validateGeneralApplicationDirectionsMandatoryParties(caseData))
            .thenReturn(List.of("Mandatory party error"));
        when(validateHearingService.validateGeneralApplicationDirectionsNoticeSelection(caseData))
            .thenReturn(List.of("Notice selection error"));
        when(validateHearingService.validateGeneralApplicationDirectionsIntervenerParties(caseData))
            .thenReturn(List.of("Intervener warning"));

        //Act
        var response = generalApplicationDirectionsMidHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).containsExactlyInAnyOrder(
            "Mandatory party error", "Notice selection error",
            "All additional hearing documents must be Word or PDF files.");
        assertThat(response.getWarnings()).containsExactly("Intervener warning");
        assertThat(response.getData()).isEqualTo(caseData);

        InOrder inOrder = inOrder(generalApplicationDirectionsService, validateHearingService);
        inOrder.verify(generalApplicationDirectionsService).isHearingRequired(callbackRequest.getCaseDetails());
        inOrder.verify(validateHearingService).hasInvalidAdditionalHearingDocs(callbackRequest.getCaseDetails().getData());
        inOrder.verify(validateHearingService).validateGeneralApplicationDirectionsMandatoryParties(caseData);
        inOrder.verify(validateHearingService).validateGeneralApplicationDirectionsNoticeSelection(caseData);
        inOrder.verify(validateHearingService).validateGeneralApplicationDirectionsIntervenerParties(caseData);
    }

    @Test
    void givenNoHearingRequired_whenHandle_thenNoValidation() {
        //Arrange
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();

        when(generalApplicationDirectionsService.isHearingRequired(caseDetails)).thenReturn(false);

        //Act
        var response = generalApplicationDirectionsMidHandler.handle(callbackRequest, "authToken");

        // Assert
        verifyNoInteractions(validateHearingService);
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }

    @Test
    void givenWarningExists_whenHandle_thenShouldNotGeneratePreviewOfGeneralApplicationDirections() {
        request = FinremCallbackRequestFactory.from();

        when(generalApplicationDirectionsService.isHearingRequired(request.getCaseDetails())).thenReturn(true);
        mockErrorsAndWarnings(false, null, null, "warning");

        var response = generalApplicationDirectionsMidHandler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verifyValidateHearingServiceMethodsInvoked(),
            () -> verifyNoDocumentGeneration(),
            () -> verifyNoMoreInteractions(validateHearingService, generalApplicationDirectionsService),
            () -> assertThat(response.getErrors()).isEmpty(),
            () -> assertThat(response.getWarnings()).containsExactly("warning")
        );
    }

    @Test
    void givenErrorExists_whenHandle_thenShouldNotGeneratePreviewOfGeneralApplicationDirections() {
        request = FinremCallbackRequestFactory.from();

        when(generalApplicationDirectionsService.isHearingRequired(request.getCaseDetails())).thenReturn(true);
        mockErrorsAndWarnings(true, "error1", "error2", null);

        var response = generalApplicationDirectionsMidHandler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verifyValidateHearingServiceMethodsInvoked(),
            () -> verifyNoDocumentGeneration(),
            () -> verifyNoMoreInteractions(validateHearingService, generalApplicationDirectionsService),
            () -> assertThat(response.getErrors()).containsExactly("All additional hearing documents must be Word or PDF files.",
                "error1", "error2"),
            () -> assertThat(response.getWarnings()).isEmpty()
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenEmptyWarningAndErrors_whenHandle_thenGeneratePreviewOfGeneralApplicationDirectionsIfNeeded(boolean documentGenerated) {
        request = FinremCallbackRequestFactory.from();

        when(generalApplicationDirectionsService.isHearingRequired(request.getCaseDetails())).thenReturn(true);
        mockErrorsAndWarnings(false, null, null, null);
        final CaseDocument actualDocument = caseDocument();
        when(generalApplicationDirectionsService
            .generateGeneralApplicationDirectionsDocumentIfNeeded(AUTH_TOKEN, request.getCaseDetails()))
            .thenReturn(documentGenerated ? Optional.empty() : Optional.of(actualDocument));

        var response = generalApplicationDirectionsMidHandler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verifyValidateHearingServiceMethodsInvoked(),
            () -> verify(generalApplicationDirectionsService).generateGeneralApplicationDirectionsDocumentIfNeeded(AUTH_TOKEN,
                request.getCaseDetails()),
            () -> verifyNoMoreInteractions(validateHearingService, generalApplicationDirectionsService),
            () -> assertThat(response.getErrors()).isEmpty(),
            () -> assertThat(response.getWarnings()).isEmpty(),
            () -> assertThat(request.getFinremCaseData())
                .extracting(FinremCaseData::getGeneralApplicationWrapper)
                .extracting(GeneralApplicationWrapper::getGeneralApplicationDirectionsPreview)
                .isEqualTo(documentGenerated ? null : actualDocument)
        );
    }

    private void verifyValidateHearingServiceMethodsInvoked() {
        assertAll(
            () -> verify(validateHearingService).hasInvalidAdditionalHearingDocs(request.getFinremCaseData()),
            () -> verify(validateHearingService).validateGeneralApplicationDirectionsMandatoryParties(request.getFinremCaseData()),
            () -> verify(validateHearingService).validateGeneralApplicationDirectionsNoticeSelection(request.getFinremCaseData()),
            () -> verify(validateHearingService).validateGeneralApplicationDirectionsIntervenerParties(request.getFinremCaseData())
        );
    }

    private void mockErrorsAndWarnings(boolean hasInvalidAdditionalHearingDocs,
                                       String error1, String error2, String warning) {
        when(validateHearingService.hasInvalidAdditionalHearingDocs(request.getFinremCaseData())).thenReturn(hasInvalidAdditionalHearingDocs);
        when(validateHearingService.validateGeneralApplicationDirectionsMandatoryParties(request.getFinremCaseData()))
            .thenReturn(error1 == null ? List.of() : List.of(error1));
        when(validateHearingService.validateGeneralApplicationDirectionsNoticeSelection(request.getFinremCaseData()))
            .thenReturn(error2 == null ? List.of() : List.of(error2));
        when(validateHearingService.validateGeneralApplicationDirectionsIntervenerParties(request.getFinremCaseData()))
            .thenReturn(warning == null ? List.of() : List.of(warning));
    }

    private void verifyNoDocumentGeneration() {
        verify(generalApplicationDirectionsService, never())
            .generateGeneralApplicationDirectionsDocumentIfNeeded(eq(AUTH_TOKEN), any(FinremCaseDetails.class));
    }
}
