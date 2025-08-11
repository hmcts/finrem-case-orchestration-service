package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificationRequestBuilderTest {

    @InjectMocks
    private NotificationRequestBuilder builder;
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    @Test
    void givenConsentedCase_whenWithDefaults_thenCaseDetailsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Doris")
                .applicantLname("Duck")
                .appRespondentFmName("Davey")
                .appRespondentLName("Duck")
                .build())
            .ccdCaseType(CaseType.CONSENTED)
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(3456865498462385L)
            .caseType(CaseType.CONSENTED)
            .data(caseData)
            .build();

        NotificationRequest notificationRequest = builder
            .withDefaults(caseDetails)
            .build();

        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("3456865498462385");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONSENTED);
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Doris Duck");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Davey Duck");
        assertThat(notificationRequest.getCaseOrderType()).isEqualTo(CaseType.CONSENTED.getCcdType());

        Set<String> excludedFields = Set.of(
            "caseReferenceNumber",
            "caseType",
            "applicantName",
            "respondentName",
            "caseOrderType"
        );
        verifyFieldsEqualNull(notificationRequest, excludedFields);
    }

    @Test
    void givenContestedCase_whenWithDefaults_thenCaseDetailsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Doris")
                .applicantLname("Duck")
                .respondentFmName("Davey")
                .respondentLname("Duck")
                .build())
            .ccdCaseType(CaseType.CONTESTED)
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(3456865498462385L)
            .caseType(CaseType.CONTESTED)
            .data(caseData)
            .build();

        NotificationRequest notificationRequest = builder
            .withDefaults(caseDetails)
            .build();

        assertThat(notificationRequest.getCaseReferenceNumber()).isEqualTo("3456865498462385");
        assertThat(notificationRequest.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(notificationRequest.getApplicantName()).isEqualTo("Doris Duck");
        assertThat(notificationRequest.getRespondentName()).isEqualTo("Davey Duck");
        assertThat(notificationRequest.getCaseOrderType()).isEqualTo(CaseType.CONTESTED.getCcdType());

        Set<String> excludedFields = Set.of(
            "caseReferenceNumber",
            "caseType",
            "applicantName",
            "respondentName",
            "caseOrderType"
        );
        verifyFieldsEqualNull(notificationRequest, excludedFields);
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

    private void verifyFieldsEqualNull(NotificationRequest notificationRequest, Set<String> excludedFields) {
        String[] nullFieldNames = Arrays.stream(NotificationRequest.class.getDeclaredFields())
            .map(Field::getName)
            .filter(name -> !excludedFields.contains(name))
            .toArray(String[]::new);

        assertThat(notificationRequest)
            .extracting(nullFieldNames)
            .containsOnlyNulls();
    }
}
