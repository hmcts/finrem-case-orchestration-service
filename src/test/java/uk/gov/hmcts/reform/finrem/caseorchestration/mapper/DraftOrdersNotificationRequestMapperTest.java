package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT;

@ExtendWith(MockitoExtension.class)
class DraftOrdersNotificationRequestMapperTest {

    @InjectMocks
    DraftOrdersNotificationRequestMapper draftOrdersNotificationRequestMapper;
    @Mock
    HearingService hearingService;
    @Mock
    CourtDetailsConfiguration courtDetailsConfiguration;

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

        NotificationRequest notificationRequest = draftOrdersNotificationRequestMapper
            .buildCaseworkerDraftOrderReviewOverdue(caseDetails, draftOrdersReview);

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
