package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT;

@ExtendWith(MockitoExtension.class)
class DraftOrdersNotificationRequestMapperTest {
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;
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

    @ParameterizedTest
    @MethodSource("buildRefusedDraftOrderOrPsaNotificationRequest")
    void testBuildRefusedDraftOrderOrPsaNotificationRequest(OrderParty orderParty, String expectedEmail) {
        FinremCaseDetails caseDetails = createCaseDetailsForRefusedOrderNotificationRequest();

        NotificationRequest notificationRequest = mapper.buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails,
            createRefusedOrder(orderParty));

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo(expectedEmail);
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("45347643533535");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Charlie Hull");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Stella Hull");
        assertThat(notificationRequest.getHearingDate()).isEqualTo("5 January 2024");
        assertThat(notificationRequest.getSelectedCourt()).isEqualTo(RegionNorthWestFrc.LIVERPOOL.getValue());
        assertThat(notificationRequest.getDocumentName()).isEqualTo("abc.pdf");
        assertThat(notificationRequest.getJudgeFeedback()).isEqualTo("Judge Feedback");
        assertThat(notificationRequest.getJudgeName()).isEqualTo("Peter Chapman");
        assertThat(notificationRequest.getName()).isEqualTo("Mr. Uploader");
        assertThat(notificationRequest.getSolicitorReferenceNumber()).isEqualTo("A_RANDOM_STRING");
    }

    private static Stream<Arguments> buildRefusedDraftOrderOrPsaNotificationRequest() {
        return Stream.of(
            Arguments.of(OrderParty.APPLICANT, "applicant@solicitor.com"),
            Arguments.of(OrderParty.RESPONDENT, "respondent@solicitor.com")
        );
    }

    private RefusedOrder createRefusedOrder(OrderParty orderParty) {
        return RefusedOrder.builder()
            .hearingDate(LocalDate.of(2024, 1, 5))
            .refusalJudge("Peter Chapman")
            .judgeFeedback("Judge Feedback")
            .submittedBy("Mr. Uploader")
            .orderParty(orderParty)
            .refusedDocument(CaseDocument.builder().documentFilename("abc.pdf").build())
            .build();
    }

    private FinremCaseDetails createCaseDetailsForRefusedOrderNotificationRequest() {
        DraftOrdersReview draftOrdersReview = createDraftOrdersReview();

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .solicitorReference("A_RANDOM_STRING")
                .applicantSolicitorEmail("applicant@solicitor.com")
                .applicantFmName("Charlie")
                .applicantLname("Hull")
                .respondentSolicitorEmail("respondent@solicitor.com")
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
        return FinremCaseDetails.builder()
            .id(45347643533535L)
            .data(caseData)
            .build();
    }
}
