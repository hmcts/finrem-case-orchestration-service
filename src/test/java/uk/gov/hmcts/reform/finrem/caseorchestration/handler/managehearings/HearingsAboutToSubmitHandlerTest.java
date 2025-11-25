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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing.transformHearingInputsToHearing;

@ExtendWith(MockitoExtension.class)
class HearingsAboutToSubmitHandlerTest {

    @Mock
    private ManageHearingActionService manageHearingActionService;

    @InjectMocks
    private ManageHearingsAboutToSubmitHandler manageHearingsAboutToSubmitHandler;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageHearingsAboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.MANAGE_HEARINGS);
    }

    @Test
    void givenValidCaseData_whenHandleAdd_thenHearingAddedToManageHearingsList() {
        // Arrange
        String caseReference = TestConstants.CASE_ID;
        WorkingHearing hearingToAdd = createHearingToAdd();

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .manageHearingsActionSelection(ManageHearingsAction.ADD_HEARING)
                .workingHearing(hearingToAdd)
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        doAnswer(invocation -> {
            UUID workingHearingID = UUID.randomUUID();
            ManageHearingsCollectionItem manageHearingsCollectionItem = ManageHearingsCollectionItem.builder()
                .id(workingHearingID)
                .value(transformHearingInputsToHearing(hearingToAdd))
                .build();

            ManageHearingDocumentsCollectionItem manageHearingDocumentsCollectionItem = ManageHearingDocumentsCollectionItem
                .builder()
                .id(UUID.randomUUID())
                .value(ManageHearingDocument.builder()
                    .hearingId(workingHearingID)
                    .hearingDocument(CaseDocument.builder()
                        .categoryId("categoryId")
                        .documentUrl("documentUrl")
                        .documentFilename("HearingNotice.pdf")
                        .documentBinaryUrl("documentBinaryUrl")
                        .uploadTimestamp(LocalDateTime.now())
                        .build())
                    .hearingCaseDocumentType(CaseDocumentType.HEARING_NOTICE)
                    .build())
                .build();

            FinremCaseDetails details = invocation.getArgument(0);
            ManageHearingsWrapper wrapper = details.getData().getManageHearingsWrapper();

            wrapper.setHearings(List.of(manageHearingsCollectionItem));
            wrapper.setHearingDocumentsCollection(List.of(manageHearingDocumentsCollectionItem));
            wrapper.setWorkingHearing(null);
            wrapper.setWorkingHearingId(workingHearingID);
            return null;
        }).when(manageHearingActionService)
            .performAddHearing(request.getCaseDetails(), AUTH_TOKEN);

        HearingTabCollectionItem hearingTabItem = HearingTabCollectionItem.builder()
            .id(UUID.randomUUID())
            .value(HearingTabItem
                .builder()
                .tabAttendance("In Person")
                .tabHearingType("hearingType")
                .tabDateTime("12 Oct 2023 10:00")
                .tabCourtSelection("Court Name")
                .tabTimeEstimate("30mins")
                .tabConfidentialParties("Applicant")
                .tabAdditionalInformation("Additional Info")
                .tabHearingDocuments(List.of(
                    DocumentCollectionItem.builder()
                        .value(CaseDocument.builder()
                            .documentUrl("documentUrl")
                            .documentFilename("HearingNotice.pdf")
                            .build())
                        .build()))
                .build())
            .build();

        doAnswer(invocation -> {
            FinremCaseData data = invocation.getArgument(0);
            ManageHearingsWrapper wrapper = data.getManageHearingsWrapper();
            wrapper.setApplicantHearingTabItems(List.of(hearingTabItem));
            return null;
        }).when(manageHearingActionService)
            .updateTabData(request.getCaseDetails().getData());

        // Act
        var response = manageHearingsAboutToSubmitHandler.handle(request, AUTH_TOKEN);
        var responseManageHearingsWrapper = response.getData().getManageHearingsWrapper();
        var hearingDocumentAdded = responseManageHearingsWrapper.getHearingDocumentsCollection().getFirst();
        var hearingId = responseManageHearingsWrapper.getHearings().getFirst().getId();

        //Assert perform add
        assertThat(responseManageHearingsWrapper.getHearings())
            .extracting(ManageHearingsCollectionItem::getValue)
            .contains(transformHearingInputsToHearing(hearingToAdd));
        assertThat(hearingDocumentAdded.getValue().getHearingId()).isEqualTo(hearingId);
        assertThat(hearingDocumentAdded.getValue().getHearingDocument().getDocumentFilename())
            .isEqualTo("HearingNotice.pdf");
        assertThat(hearingDocumentAdded.getValue().getHearingCaseDocumentType()).isEqualTo(CaseDocumentType.HEARING_NOTICE);
        assertThat(responseManageHearingsWrapper.getWorkingHearingId()).isEqualTo(hearingId);
        assertThat(responseManageHearingsWrapper.getWorkingHearing()).isNull();
        assertStateIsPrepareForHearing(response.getData());

        //Assert perform tab data
        assertThat(responseManageHearingsWrapper.getApplicantHearingTabItems())
            .contains(hearingTabItem);
    }

    @Test
    void givenValidCaseData_whenHandleVacate_thenPerformVacateHearingCalled() {
        String caseReference = TestConstants.CASE_ID;

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .manageHearingsActionSelection(ManageHearingsAction.VACATE_HEARING)
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(
            Long.parseLong(caseReference), CaseType.CONTESTED, caseData);

        manageHearingsAboutToSubmitHandler.handle(request, AUTH_TOKEN);

        verify(manageHearingActionService).performVacateHearing(request.getCaseDetails());
    }

    private WorkingHearing createHearingToAdd() {
        return WorkingHearing
            .builder()
            .hearingDate(LocalDate.now())
            .hearingTypeDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(HearingType.DIR.name())
                    .label(HearingType.DIR.getId())
                    .build())
                .build())
            .hearingTimeEstimate("30mins")
            .hearingTime("10:00")
            .hearingMode(HearingMode.IN_PERSON)
            .additionalHearingInformation("Additional Info")
            .hearingNoticePrompt(YesOrNo.YES)
            .additionalHearingDocPrompt(YesOrNo.YES)
            .additionalHearingDocs(List.of(
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

    /**
     * Checks that the value is correct.  And that the expected constant/alias resolves to the same value.
     */
    private void assertStateIsPrepareForHearing(FinremCaseData caseData) {
        assertThat(caseData.getState()).isEqualTo("prepareForHearing");
        assertThat(caseData.getState()).isEqualTo(PREPARE_FOR_HEARING.getId());
    }
}
