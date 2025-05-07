package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageHearingsAboutToSubmitHandlerTest {

    @InjectMocks
    private ManageHearingsAboutToSubmitHandler manageHearingsAboutToSubmitHandler;

    @Mock
    private ValidateHearingService validateHearingService;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageHearingsAboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.MANAGE_HEARINGS);
    }

    @Test
    void givenValidCaseData_whenHandle_thenReturnsResponseWithErrorsAndWarnings() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .workingManageHearing(ManageHearing.builder()
                    .manageHearingType(ManageHearingType.DIR)
                    .build())
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(finremCaseData)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(validateHearingService.validateManageHearingErrors(finremCaseData))
            .thenReturn(List.of("Error 1", "Error 2"));
        when(validateHearingService.validateManageHearingWarnings(finremCaseData, ManageHearingType.DIR))
            .thenReturn(List.of("Warning 1"));

        // Act
        var response = manageHearingsAboutToSubmitHandler.handle(callbackRequest, "authToken");

        // Assert
        assertThat(response.getErrors()).containsExactly("Error 1", "Error 2");
        assertThat(response.getWarnings()).containsExactly("Warning 1");
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }

    @Test
    void givenValidCaseData_whenHandle_thenHearingAddedToManageHearingsList() {
        String caseReference = TestConstants.CASE_ID;
        ManageHearing hearingToAdd = createHearingToAdd();

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .workingManageHearing(hearingToAdd)
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        var response = manageHearingsAboutToSubmitHandler.handle(request, AUTH_TOKEN);
        var responseManageHearingsWrapper = response.getData().getManageHearingsWrapper();

        assertThat(responseManageHearingsWrapper.getManageHearings())
            .extracting(ManageHearingsCollectionItem::getValue)
            .contains(hearingToAdd);
        assertThat(responseManageHearingsWrapper.getWorkingManageHearing()).isNull();
        assertThat(responseManageHearingsWrapper.getManageHearingsActionSelection()).isNull();
    }

    private ManageHearing createHearingToAdd() {
        return ManageHearing
            .builder()
            .manageHearingDate(LocalDate.now())
            .manageHearingType(ManageHearingType.DIR)
            .manageHearingTimeEstimate("30mins")
            .manageHearingTime("10:00")
            .manageHearingMode(HearingMode.IN_PERSON)
            .manageHearingAdditionalInformation("Additional Info")
            .manageHearingNoticePrompt(YesOrNo.YES)
            .manageHearingAdditionalDocPrompt(YesOrNo.YES)
            .manageHearingUploadAdditionalDocs(List.of(
                DocumentCollectionItem
                    .builder()
                    .value(CaseDocument
                        .builder()
                        .categoryId("categoryId")
                        .documentUrl("documentUrl")
                        .documentFilename("documentFileName")
                        .documentBinaryUrl("documentBinaryUrl")
                        .uploadTimestamp(LocalDateTime.now())
                        .build())
                    .build()
            ))
            .partiesOnCaseMultiSelectList(DynamicMultiSelectList
                .builder()
                .value(List.of(DynamicMultiSelectListElement
                    .builder()
                    .label("Party1")
                    .code("Party1")
                    .build()))
                .listItems(List.of(DynamicMultiSelectListElement
                    .builder()
                    .label("Party1")
                    .code("Party1")
                    .build()))
                .build())
            .build();
    }

}