package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAERNARFON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc.NORTH_WALES;

@ExtendWith(MockitoExtension.class)
class NotificationRequestBuilderTest {

    @InjectMocks
    private NotificationRequestBuilder builder;
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;
    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Test
    void givenConsentedCase_whenWithCaseDefaults_thenCaseDetailsPopulated() {
        mockCourtDetailsConfiguration();
        mockConsentedVariationOrder(false);
        FinremCaseDetails caseDetails = createConsentedCase();

        NotificationRequest notificationRequest = builder
            .withCaseDefaults(caseDetails)
            .build();

        assertThat(notificationRequest.getApplicantName()).isEqualTo("Doris Duck");
        assertThat(notificationRequest.getCamelCaseOrderType()).isEqualTo("Consent");
        assertThat(notificationRequest.getCaseOrderType()).isEqualTo("consent");
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("3456865498462385");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONSENTED);
        assertThat(notificationRequest.getContactCourtName()).isEqualTo("Test Court");
        assertThat(notificationRequest.getContactCourtEmail()).isEqualTo("test.court@test.com");
        assertThat(notificationRequest.getDivorceCaseNumber()).isEqualTo("3657-4535-2355-7545");
        assertThat(notificationRequest.getEmailReplyToId()).isEqualTo("3f6a1cd4-812a-48d6-8fd9-067bf4884fd2");
        assertThat(notificationRequest.getPhoneOpeningHours()).isEqualTo(CTSC_OPENING_HOURS);
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Davey Duck");
        assertThat(notificationRequest.getSelectedCourt()).isNull();
    }

    @Test
    void givenConsentedVariationOrderCase_whenWithCaseDefaults_thenCaseDetailsPopulated() {
        mockCourtDetailsConfiguration();
        mockConsentedVariationOrder(true);
        FinremCaseDetails caseDetails = createConsentedCase();

        NotificationRequest notificationRequest = builder
            .withCaseDefaults(caseDetails)
            .build();

        assertThat(notificationRequest.getApplicantName()).isEqualTo("Doris Duck");
        assertThat(notificationRequest.getCamelCaseOrderType()).isEqualTo("Variation");
        assertThat(notificationRequest.getCaseOrderType()).isEqualTo("variation");
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("3456865498462385");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONSENTED);
        assertThat(notificationRequest.getContactCourtName()).isEqualTo("Test Court");
        assertThat(notificationRequest.getContactCourtEmail()).isEqualTo("test.court@test.com");
        assertThat(notificationRequest.getDivorceCaseNumber()).isEqualTo("3657-4535-2355-7545");
        assertThat(notificationRequest.getEmailReplyToId()).isEqualTo("3f6a1cd4-812a-48d6-8fd9-067bf4884fd2");
        assertThat(notificationRequest.getPhoneOpeningHours()).isEqualTo(CTSC_OPENING_HOURS);
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Davey Duck");
        assertThat(notificationRequest.getSelectedCourt()).isNull();
    }

    @Test
    void givenContestedCase_whenWithDefaults_thenCaseDetailsPopulated() {
        mockCourtDetailsConfiguration();
        FinremCaseDetails caseDetails = createContestedCase();

        NotificationRequest notificationRequest = builder
            .withCaseDefaults(caseDetails)
            .build();

        assertThat(notificationRequest.getApplicantName()).isEqualTo("Doris Duck");
        assertThat(notificationRequest.getCamelCaseOrderType()).isNull();
        assertThat(notificationRequest.getCaseOrderType()).isNull();
        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("3456865498462385");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(notificationRequest.getContactCourtName()).isEqualTo("Test Court");
        assertThat(notificationRequest.getContactCourtEmail()).isEqualTo("test.court@test.com");
        assertThat(notificationRequest.getDivorceCaseNumber()).isEqualTo("3657-4535-2355-7545");
        assertThat(notificationRequest.getEmailReplyToId()).isEqualTo("3f6a1cd4-812a-48d6-8fd9-067bf4884fd2");
        assertThat(notificationRequest.getPhoneOpeningHours()).isEqualTo(CTSC_OPENING_HOURS);
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Davey Duck");
        assertThat(notificationRequest.getSelectedCourt()).isEqualTo(NORTH_WALES.getValue());
    }

    @Test
    void givenAllPropertiesSet_whenBuild_thenAllPropertiesPopulated() {
        byte[] documentContents = {1, 2, 3};
        NotificationRequest notificationRequest = builder
            .caseReferenceNumber("123456789")
            .solicitorReferenceNumber("SOL123")
            .divorceCaseNumber("DIV123")
            .name("John Doe")
            .notificationEmail("john.doe@example.com")
            .selectedCourt("London Court")
            .caseType("consented")
            .generalEmailBody("Email body")
            .phoneOpeningHours("9-5")
            .caseOrderType("Consent Order")
            .camelCaseOrderType("ConsentOrder")
            .generalApplicationRejectionReason("Missing docs")
            .applicantName("Jane Smith")
            .respondentName("Richard Roe")
            .barristerReferenceNumber("BAR123")
            .hearingType("Directions")
            .intervenerSolicitorReferenceNumber("INT123")
            .intervenerFullName("Intervener Name")
            .intervenerSolicitorFirm("Intervener Firm")
            .documentContents(documentContents)
            .isNotDigital(Boolean.TRUE)
            .hearingDate("2024-06-01")
            .judgeName("Judge Judy")
            .oldestDraftOrderDate("2024-05-01")
            .judgeFeedback("Approved")
            .documentName("Order.pdf")
            .contactCourtEmail("test.court@test.net")
            .contactCourtName("Local Court")
            .emailReplyToId("909cb736-eab0-46ac-a7f0-d28d89c8950c")
            .build();

        // Assert all fields are non-null
        String[] fieldNames = Arrays.stream(NotificationRequest.class.getDeclaredFields())
            .map(Field::getName)
            .toArray(String[]::new);

        assertThat(notificationRequest)
            .extracting(fieldNames)
            .doesNotContainNull();

        // For array fields, check content equality
        assertThat(notificationRequest.getDocumentContents()).isEqualTo(documentContents);
    }

    @Test
    void givenSolicitorData_whenWithSolicitorCaseData_thenSolicitorDetailsPopulated() {
        SolicitorCaseDataKeysWrapper solicitorCaseData = SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey("sol@test.com")
            .solicitorNameKey("Stella Artois")
            .solicitorReferenceKey("DGEE34343")
            .solicitorIsNotDigitalKey(true)
            .build();

        NotificationRequest notificationRequest = builder
            .withSolicitorCaseData(solicitorCaseData)
            .build();

        assertThat(notificationRequest.getName()).isEqualTo("Stella Artois");
        assertThat(notificationRequest.getNotificationEmail()).isEqualTo("sol@test.com");
        assertThat(notificationRequest.getSolicitorReferenceNumber()).isEqualTo("DGEE34343");
        assertThat(notificationRequest.getIsNotDigital()).isTrue();
    }

    @Test
    void givenMissingSolicitorData_whenWithSolicitorCaseData_thenSolicitorDetailsNotPopulated() {
        SolicitorCaseDataKeysWrapper solicitorCaseData = SolicitorCaseDataKeysWrapper.builder()
            .build();

        NotificationRequest notificationRequest = builder
            .withSolicitorCaseData(solicitorCaseData)
            .build();

        assertThat(notificationRequest.getName()).isEmpty();
        assertThat(notificationRequest.getNotificationEmail()).isEmpty();
        assertThat(notificationRequest.getSolicitorReferenceNumber()).isEmpty();
        assertThat(notificationRequest.getIsNotDigital()).isNull();
    }

    @Test
    void givenCase_whenWithCourtAsEmailDestination_thenNotificationEmailPopulated() {
        mockCourtDetailsConfiguration();
        FinremCaseDetails caseDetails = createContestedCase();

        NotificationRequest notificationRequest = builder
            .withCourtAsEmailDestination(caseDetails)
            .build();

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo("test.court@test.com");
    }

    private FinremCaseDetails createConsentedCase() {
        FinremCaseData caseData = FinremCaseData.builder()
            .regionWrapper(createRegionWrapper())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Doris")
                .applicantLname("Duck")
                .appRespondentFmName("Davey")
                .appRespondentLName("Duck")
                .build())
            .ccdCaseType(CaseType.CONSENTED)
            .divorceCaseNumber("3657-4535-2355-7545")
            .build();
        return FinremCaseDetails.builder()
            .id(3456865498462385L)
            .caseType(CaseType.CONSENTED)
            .data(caseData)
            .build();
    }

    private FinremCaseDetails createContestedCase() {
        FinremCaseData caseData = FinremCaseData.builder()
            .regionWrapper(createRegionWrapper())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Doris")
                .applicantLname("Duck")
                .respondentFmName("Davey")
                .respondentLname("Duck")
                .build())
            .ccdCaseType(CaseType.CONTESTED)
            .divorceCaseNumber("3657-4535-2355-7545")
            .build();
        return FinremCaseDetails.builder()
            .id(3456865498462385L)
            .caseType(CaseType.CONTESTED)
            .data(caseData)
            .build();
    }

    private RegionWrapper createRegionWrapper() {
        return RegionWrapper.builder()
            .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                .regionList(Region.WALES)
                .walesFrcList(NORTH_WALES)
                .courtListWrapper(DefaultCourtListWrapper.builder()
                    .northWalesCourtList(NorthWalesCourt.CAERNARFON)
                    .build())
                .build())
            .build();
    }

    private void mockCourtDetailsConfiguration() {
        CourtDetails courtDetails = CourtDetails.builder()
            .courtName("Test Court")
            .email("test.court@test.com")
            .emailReplyToId("3f6a1cd4-812a-48d6-8fd9-067bf4884fd2")
            .build();
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of(CAERNARFON, courtDetails));
    }

    private void mockConsentedVariationOrder(boolean variationOrder) {
        when(consentedApplicationHelper.isVariationOrder(any(FinremCaseData.class))).thenReturn(variationOrder);
    }
}
