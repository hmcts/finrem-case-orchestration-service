package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftOrdersNotificationRequestMapperTest {

    @Mock
    HearingService hearingService;

    @InjectMocks
    private DraftOrdersNotificationRequestMapper mapper;

    @Test
    void shouldBuildJudgeNotificationRequestWithValidData() {
        FinremCaseDetails caseDetails = createCaseDetails();

        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.of(1999, 8, 6));

        NotificationRequest result = mapper.buildJudgeNotificationRequest(caseDetails);

        verify(hearingService).getHearingDate(any(), any());
        assertEquals("123456789", result.getCaseReferenceNumber());
        assertEquals("6 August 1999", result.getHearingDate());
        assertEquals("judge@test.com", result.getNotificationEmail());
        assertEquals("Hamzah Tahir", result.getApplicantName());
        assertEquals("Anne Taylor", result.getRespondentName());
    }

    private FinremCaseDetails createCaseDetails() {
        Long caseReference = 123456789L;
        String applicantFirstName = "Hamzah";
        String applicantLastName = "Tahir";
        String respondentFirstName = "Anne";
        String respondentLastName = "Taylor";
        String judgeEmail = "judge@test.com";

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .applicantFmName(applicantFirstName)
            .applicantLname(applicantLastName)
            .respondentFmName(respondentFirstName)
            .respondentLname(respondentLastName)
            .build();

        LocalDate hearingDate = LocalDate.of(1999, 8, 6);
        DynamicListElement selectedElement = DynamicListElement.builder()
            .label("Hearing Date")
            .code(hearingDate.toString())
            .build();

        DynamicList hearingDetails = DynamicList.builder()
            .value(selectedElement)
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                    .judge(judgeEmail)
                    .hearingDetails(hearingDetails)
                    .build())
                .build())
            .build();

        caseData.setCcdCaseType(CaseType.CONTESTED);

        return FinremCaseDetails.builder()
            .id(caseReference)
            .data(caseData)
            .build();
    }
}
