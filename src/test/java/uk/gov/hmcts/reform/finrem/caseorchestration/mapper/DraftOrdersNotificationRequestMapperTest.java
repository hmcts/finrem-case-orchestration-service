package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT;

@ExtendWith(MockitoExtension.class)
class DraftOrdersNotificationRequestMapperTest {

    @Mock
    HearingService hearingService;
    @Mock
    CourtDetailsConfiguration courtDetailsConfiguration;
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

    @Test
    void testBuildCaseworkerDraftOrderReviewOverdue() {
        CourtDetails courtDetails = mock(CourtDetails.class);
        when(courtDetails.getEmail()).thenReturn("receipient@test.com");
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of(
            LIVERPOOL_CIVIL_FAMILY_COURT.getSelectedCourtId(), courtDetails));

        DraftOrdersReview draftOrdersReview = createDraftOrdersReview();

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Charlie")
                .applicantLname("Hull")
                .respondentFmName("Stella")
                .respondentLname("Hull")
                .build())
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .courtListWrapper(DefaultCourtListWrapper.builder()
                        .liverpoolCourtList(LIVERPOOL_CIVIL_FAMILY_COURT).build())
                    .regionList(Region.NORTHWEST)
                    .northWestFrcList(RegionNorthWestFrc.LIVERPOOL)
                    .build())
                .build())
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(draftOrdersReview)
                        .build()
                ))
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(45347643533535L)
            .data(caseData)
            .build();

        NotificationRequest notificationRequest = mapper.buildCaseworkerDraftOrderReviewOverdue(caseDetails,
            draftOrdersReview);

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo("receipient@test.com");
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("45347643533535");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Charlie Hull");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Stella Hull");
        assertThat(notificationRequest.getHearingDate()).isEqualTo("5 January 2024");
        assertThat(notificationRequest.getSelectedCourt()).isEqualTo(RegionNorthWestFrc.LIVERPOOL.getValue());
        assertThat(notificationRequest.getJudgeName()).isEqualTo("judge@test.com");
        assertThat(notificationRequest.getOldestDraftOrderDate()).isEqualTo("10 January 2024");
    }

    private DraftOrdersReview createDraftOrdersReview() {
        DraftOrderDocReviewCollection draftOrderDocReviewCollection = DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                .submittedDate(LocalDateTime.of(2024, 1, 10, 13, 12))
                .build()
            )
            .build();
        return DraftOrdersReview.builder()
            .hearingDate(LocalDate.of(2024,1,5))
            .hearingJudge("judge@test.com")
            .draftOrderDocReviewCollection(List.of(
                draftOrderDocReviewCollection
            ))
            .build();
    }
}
