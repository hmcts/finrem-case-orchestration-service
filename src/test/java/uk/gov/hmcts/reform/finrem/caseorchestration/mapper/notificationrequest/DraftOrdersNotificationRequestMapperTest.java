package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.APPLICANT_BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class DraftOrdersNotificationRequestMapperTest {
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;
    @Mock
    private NotificationRequestBuilderFactory notificationRequestBuilderFactory;
    @InjectMocks
    private DraftOrdersNotificationRequestMapper mapper;

    @BeforeEach
    void setUp() {
        lenient().when(notificationRequestBuilderFactory.newInstance())
            .thenReturn(new NotificationRequestBuilder(courtDetailsConfiguration));
    }

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

    @Test
    void testBuildAdminNotificationRequest() {
        mockCourtDetailsConfiguration();
        FinremCaseDetails caseDetails = createCaseDetails();

        NotificationRequest notificationRequest = mapper.buildAdminReviewNotificationRequest(caseDetails,
            LocalDate.of(2025, 1, 17));

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo("receipient@test.com");
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("123456789");
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Hamzah Tahir");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Anne Taylor");
        assertThat(notificationRequest.getHearingDate()).isEqualTo("17 January 2025");
    }

    @Test
    void testBuildCaseworkerDraftOrderReviewOverdue() {
        mockCourtDetailsConfiguration();
        DraftOrdersReview draftOrdersReview = createDraftOrdersReview();
        FinremCaseDetails caseDetails = createCaseDetails(draftOrdersReview);

        NotificationRequest notificationRequest = mapper.buildCaseworkerDraftOrderReviewOverdue(caseDetails,
            draftOrdersReview);

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo("receipient@test.com");
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("123456789");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Hamzah Tahir");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Anne Taylor");
        assertThat(notificationRequest.getHearingDate()).isEqualTo("5 January 2024");
        assertThat(notificationRequest.getSelectedCourt()).isEqualTo(RegionNorthWestFrc.LIVERPOOL.getValue());
        assertThat(notificationRequest.getJudgeName()).isEqualTo("judge@test.com");
        assertThat(notificationRequest.getOldestDraftOrderDate()).isEqualTo("10 January 2024");
    }

    @Test
    void testBuildRefusedDraftOrderOrPsaNotificationRequest() {
        FinremCaseDetails caseDetails = createCaseDetails(createDraftOrdersReview());

        NotificationRequest notificationRequest = mapper.buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails,
            RefusedOrder.builder()
                .hearingDate(LocalDate.of(2024, 1, 5))
                .refusalJudge("Peter Chapman")
                .judgeFeedback("Judge Feedback")
                .submittedBy("Mr. Uploader")
                .submittedByEmail("hello@world.com")
                .refusedDocument(CaseDocument.builder().documentFilename("abc.pdf").build())
                .build());

        verifyRefusedDraftOrderOrPsaNotificationRequest(notificationRequest, "hello@world.com", "Mr. Uploader");
    }

    @Test
    void givenDraftOrderUploadedOnBehalfOfApplicant_whenBuildRefusedDraftOrderOrPsaNotificationRequest_thenReturnNotificationRequest() {
        FinremCaseDetails caseDetails = createCaseDetails(createDraftOrdersReview(UPLOAD_PARTY_APPLICANT));

        NotificationRequest notificationRequest = mapper.buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails,
            RefusedOrder.builder()
                .hearingDate(LocalDate.of(2024, 1, 5))
                .refusalJudge("Peter Chapman")
                .judgeFeedback("Judge Feedback")
                .submittedBy("Mr. Uploader")
                .orderFiledBy(APPLICANT)
                .refusedDocument(CaseDocument.builder().documentFilename("abc.pdf").build())
                .build());

        verifyRefusedDraftOrderOrPsaNotificationRequest(notificationRequest, "app@solicitor.com", "Applicant Solicitor");
    }

    @Test
    void givenDraftOrderUploadedOnBehalfOfRespondent_whenBuildRefusedDraftOrderOrPsaNotificationRequest_thenReturnNotificationRequest() {
        FinremCaseDetails caseDetails = createCaseDetails(createDraftOrdersReview(UPLOAD_PARTY_RESPONDENT));

        NotificationRequest notificationRequest = mapper.buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails,
            RefusedOrder.builder()
                .hearingDate(LocalDate.of(2024, 1, 5))
                .refusalJudge("Peter Chapman")
                .judgeFeedback("Judge Feedback")
                .submittedBy("Mr. Uploader")
                .orderFiledBy(RESPONDENT)
                .refusedDocument(CaseDocument.builder().documentFilename("abc.pdf").build())
                .build());

        verifyRefusedDraftOrderOrPsaNotificationRequest(notificationRequest, "resp@solicitor.com", "Respondent Solicitor");
    }

    @Test
    void givenDraftOrderWithNoSubmittedEmail_whenBuildRefusedDraftOrderOrPsaNotificationRequest_thenThrowsException() {
        FinremCaseDetails caseDetails = createCaseDetails(createDraftOrdersReview());
        RefusedOrder refusedOrder = RefusedOrder.builder()
            .hearingDate(LocalDate.of(2024, 1, 5))
            .refusalJudge("Peter Chapman")
            .judgeFeedback("Judge Feedback")
            .submittedBy("Mr. Uploader")
            .orderFiledBy(APPLICANT_BARRISTER)
            .refusedDocument(CaseDocument.builder().documentFilename("abc.pdf").build())
            .build();

        assertThatThrownBy(() -> mapper.buildRefusedDraftOrderOrPsaNotificationRequest(caseDetails, refusedOrder))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Case ID 123456789: No refused order recipient email address found");
    }

    private void verifyRefusedDraftOrderOrPsaNotificationRequest(NotificationRequest notificationRequest,
                                                                 String expectedNotificationEmail,
                                                                 String expectedName) {
        assertThat(notificationRequest.getNotificationEmail()).isEqualTo(expectedNotificationEmail);
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("123456789");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Hamzah Tahir");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Anne Taylor");
        assertThat(notificationRequest.getHearingDate()).isEqualTo("5 January 2024");
        assertThat(notificationRequest.getSelectedCourt()).isEqualTo(RegionNorthWestFrc.LIVERPOOL.getValue());
        assertThat(notificationRequest.getDocumentName()).isEqualTo("abc.pdf");
        assertThat(notificationRequest.getJudgeFeedback()).isEqualTo("Judge Feedback");
        assertThat(notificationRequest.getJudgeName()).isEqualTo("Peter Chapman");
        assertThat(notificationRequest.getName()).isEqualTo(expectedName);
        assertThat(notificationRequest.getSolicitorReferenceNumber()).isEqualTo("A_RANDOM_STRING");
    }

    private FinremCaseDetails createCaseDetails(DraftOrdersReview draftOrdersReview) {
        FinremCaseDetails caseDetails = createCaseDetails();
        caseDetails.getData().getDraftOrdersWrapper().setDraftOrdersReviewCollection(List.of(
            DraftOrdersReviewCollection.builder()
                .value(draftOrdersReview)
                .build())
        );
        return caseDetails;
    }

    private FinremCaseDetails createCaseDetails() {
        Long caseReference = 123456789L;
        String applicantFirstName = "Hamzah";
        String applicantLastName = "Tahir";
        String respondentFirstName = "Anne";
        String respondentLastName = "Taylor";
        String judgeEmail = "judge@test.com";

        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .solicitorReference("A_RANDOM_STRING")
            .applicantSolicitorEmail("app@solicitor.com")
            .applicantSolicitorName("Applicant Solicitor")
            .applicantFmName(applicantFirstName)
            .applicantLname(applicantLastName)
            .respondentFmName(respondentFirstName)
            .respondentLname(respondentLastName)
            .respondentSolicitorEmail("resp@solicitor.com")
            .respondentSolicitorName("Respondent Solicitor")
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
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(contactDetailsWrapper)
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                    .judge(judgeEmail)
                    .hearingDetails(hearingDetails)
                    .build())
                .build())
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .courtListWrapper(DefaultCourtListWrapper.builder()
                        .liverpoolCourtList(LIVERPOOL_CIVIL_FAMILY_COURT).build())
                    .regionList(Region.NORTHWEST)
                    .northWestFrcList(RegionNorthWestFrc.LIVERPOOL)
                    .build())
                .build())
            .build();

        return FinremCaseDetails.builder()
            .id(caseReference)
            .caseType(CaseType.CONTESTED)
            .data(caseData)
            .build();
    }

    private DraftOrdersReview createDraftOrdersReview() {
        return createDraftOrdersReview(null);
    }

    private DraftOrdersReview createDraftOrdersReview(String uploadedOnBehalfOf) {
        DraftOrderDocReviewCollection draftOrderDocReviewCollection = DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                .submittedDate(LocalDateTime.of(2024, 1, 10, 13, 12))
                .uploadedOnBehalfOf(uploadedOnBehalfOf)
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

    private void mockCourtDetailsConfiguration() {
        CourtDetails courtDetails = mock(CourtDetails.class);
        when(courtDetails.getEmail()).thenReturn("receipient@test.com");
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of(
            LIVERPOOL_CIVIL_FAMILY_COURT.getSelectedCourtId(), courtDetails));
    }
}
