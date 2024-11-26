package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DraftOrdersNotificationRequestMapperTest {

    @InjectMocks
    private DraftOrdersNotificationRequestMapper mapper;

    @Test
    void shouldBuildJudgeNotificationRequestWithValidData() {
        FinremCaseDetails caseDetails = createCaseDetails();
        LocalDate hearingDate = LocalDate.of(2024, 11, 10);
        String judge = "judge@test.com";

        NotificationRequest result = mapper.buildJudgeNotificationRequest(caseDetails, hearingDate, judge);

        assertEquals("123456789", result.getCaseReferenceNumber());
        assertEquals("10 November 2024", result.getHearingDate());
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
