package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.InvalidEmailAddressException;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.SendEmailException;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.NotificationConstants.PHONE_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.APPLICANT_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.BARRISTER_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.INTERVENER_SOLICITOR_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_ADDED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_REMOVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL_ATTACHMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_OR_PSA_REFUSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_ADDED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_REMOVED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_SOLICITOR_ADDED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_INTERVENER_SOLICITOR_REMOVED_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@Slf4j
@Category(IntegrationTest.class)
public class EmailServiceTest {

    @MockitoBean
    private EmailClient mockClient;

    @Autowired
    private EmailService emailService;

    @Value("${finrem.manageCase.baseurl}")
    private String manageCaseBaseUrl;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateFieldsArgumentCaptor;

    private NotificationRequest notificationRequest;

    @Before
    public void setUp() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setNotificationEmail(TEST_SOLICITOR_EMAIL);
        notificationRequest.setCaseReferenceNumber(TEST_CASE_FAMILY_MAN_ID);
        notificationRequest.setSolicitorReferenceNumber(TEST_SOLICITOR_REFERENCE);
        notificationRequest.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        notificationRequest.setName(TEST_SOLICITOR_NAME);
        notificationRequest.setPhoneOpeningHours(PHONE_OPENING_HOURS);
        notificationRequest.setApplicantName(APPLICANT_NAME);
        notificationRequest.setRespondentName(RESPONDENT_NAME);
        notificationRequest.setBarristerReferenceNumber(BARRISTER_REFERENCE_NUMBER);
        notificationRequest.setIntervenerSolicitorReferenceNumber(INTERVENER_SOLICITOR_REFERENCE_NUMBER);
    }

    private void setConsentedData() {
        notificationRequest.setCaseType(CONSENTED);
    }

    private void setContestedData() {
        notificationRequest.setCaseType(CONTESTED);
        notificationRequest.setSelectedCourt("nottingham");
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailConsented() throws NotificationClientException {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_HWF_SUCCESSFUL.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
        assertEquals(APPLICANT_NAME, returnedTemplateVars.get("applicantName"));
        assertEquals(RESPONDENT_NAME, returnedTemplateVars.get("respondentName"));

        returnedTemplateVars.putAll(emailTemplateVars.get(FR_HWF_SUCCESSFUL.name()));
        emailService.sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_HWF_SUCCESSFUL.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailContested() throws NotificationClientException {
        setContestedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertEquals(APPLICANT_NAME, returnedTemplateVars.get("applicantName"));
        assertEquals(RESPONDENT_NAME, returnedTemplateVars.get("respondentName"));
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONTESTED_HWF_SUCCESSFUL.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_HWF_SUCCESSFUL.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailContestedForNewFRc() throws NotificationClientException {
        notificationRequest.setCaseType(CONTESTED);
        notificationRequest.setSelectedCourt("dorset");

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL.name());

        assertEquals("Dorset and Hampshire FRC", returnedTemplateVars.get("courtName"));
        assertEquals("BournemouthFRC.bournemouth.countycourt@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertEquals(APPLICANT_NAME, returnedTemplateVars.get("applicantName"));
        assertEquals(RESPONDENT_NAME, returnedTemplateVars.get("respondentName"));
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONTESTED_HWF_SUCCESSFUL.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_HWF_SUCCESSFUL.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendHwfSuccessfulConfirmationEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        setConsentedData();
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        try {
            emailService.sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void sendAssignedToJudgeConfirmationEmailConsented() throws NotificationClientException {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_ASSIGNED_TO_JUDGE.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_ASSIGNED_TO_JUDGE.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_ASSIGNED_TO_JUDGE.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendPrepareForHearingConfirmationEmailContested() throws NotificationClientException {
        setContestedData();
        notificationRequest.setHearingType("First Directions Appointment (FDA)");
        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertNull(returnedTemplateVars.get("generalEmailBody"));
        assertEquals("First Directions Appointment (FDA)", returnedTemplateVars.get("hearingType"));
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONTESTED_PREPARE_FOR_HEARING.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_PREPARE_FOR_HEARING.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendPrepareForHearingConfirmationEmailContestedIntervener() throws NotificationClientException {
        setContestedData();
        notificationRequest.setHearingType("First Directions Appointment (FDA)");
        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest,
            FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertNull(returnedTemplateVars.get("generalEmailBody"));
        assertEquals("First Directions Appointment (FDA)", returnedTemplateVars.get("hearingType"));
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendAssignedToJudgeConfirmationEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        setConsentedData();
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());
        try {
            emailService.sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmail() throws NotificationClientException {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONSENT_ORDER_MADE.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONSENT_ORDER_MADE.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONSENT_ORDER_MADE.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendVariationOrderMadeConfirmationEmail() throws NotificationClientException {
        setConsentedData();
        notificationRequest.setCaseOrderType("variation");
        notificationRequest.setCamelCaseOrderType("Variation");

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONSENT_ORDER_MADE.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONSENT_ORDER_MADE.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONSENT_ORDER_MADE.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendConsentOrderMadeConfirmationEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        setConsentedData();
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());
        try {
            emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void sendConsentOrderNotApprovedEmail() throws NotificationClientException {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONSENT_ORDER_NOT_APPROVED.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONSENT_ORDER_NOT_APPROVED.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendConsentOrderNotApprovedEmailSent() throws NotificationClientException {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONSENT_ORDER_NOT_APPROVED_SENT.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONSENT_ORDER_NOT_APPROVED_SENT.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendConsentOrderNotApprovedEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        setConsentedData();
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());
        try {
            emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void sendConsentOrderAvailableEmail() throws NotificationClientException {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONSENT_ORDER_AVAILABLE.name());

        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONSENT_ORDER_AVAILABLE.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONSENT_ORDER_AVAILABLE.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendContestedGeneralApplicationReferToJudgeEmail() throws NotificationClientException {
        setContestedData();

        Map<String, Object> returnedTemplateVars =
            emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE.name());

        returnedTemplateVars.putAll(emailTemplateVars.get(FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE.name()));

        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE.name())),
            eq(TEST_SOLICITOR_EMAIL),
            eq(returnedTemplateVars),
            anyString(), isNull());
    }

    @Test
    public void sendConsentOrderAvailableEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        setConsentedData();
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());
        try {
            emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void shouldBuildTemplateVarsForContested() {
        setContestedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertNull(returnedTemplateVars.get("generalEmailBody"));
    }

    @Test
    public void shouldBuildTemplateVarsWithCourtDataForContested() {
        setContestedData();
        notificationRequest.setContactCourtEmail("contact.court@test.com");
        notificationRequest.setContactCourtName("Local Court");

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL.name());

        assertEquals("Local Court", returnedTemplateVars.get("courtName"));
        assertEquals("contact.court@test.com", returnedTemplateVars.get("courtEmail"));
        assertNull(returnedTemplateVars.get("generalEmailBody"));
    }

    @Test
    public void shouldBuildTemplateVarsForConsented() {
        setConsentedData();

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_HWF_SUCCESSFUL.name());

        assertContestedTemplateVariablesAreAbsent(returnedTemplateVars);
    }

    @Test
    public void shouldBuildTemplateVarsWithCourtDataForConsented() {
        setConsentedData();
        notificationRequest.setContactCourtEmail("contact.court@test.com");
        notificationRequest.setContactCourtName("Local Court");

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_HWF_SUCCESSFUL.name());

        assertEquals("Local Court", returnedTemplateVars.get("courtName"));
        assertEquals("contact.court@test.com", returnedTemplateVars.get("courtEmail"));
    }

    @Test
    public void shouldBuildTemplateVarsForGeneralEmailContested() {
        setContestedData();
        notificationRequest.setGeneralEmailBody("test email body");

        Map<String, Object> returnedTemplateVars = emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_GENERAL_EMAIL.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertEquals("test email body", returnedTemplateVars.get("generalEmailBody"));
    }

    @Test
    public void shouldBuildTemplateVarsForGeneralApplicationReferToJudge() {
        setContestedData();
        notificationRequest.setGeneralEmailBody("test email body");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
        assertEquals("test email body", returnedTemplateVars.get("generalEmailBody"));
    }

    @Test
    public void shouldBuildTemplateVarsForGeneralEmailConsented() {
        setConsentedData();
        notificationRequest.setGeneralEmailBody("test email body");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_CONSENT_GENERAL_EMAIL.name());

        assertNull(returnedTemplateVars.get("courtName"));
        assertNull(returnedTemplateVars.get("courtEmail"));
        assertEquals("test email body", returnedTemplateVars.get("generalEmailBody"));
    }

    @Test
    public void shouldBuildTemplateVarsForGeneralEmailAttachmentConsented() {
        setConsentedData();
        notificationRequest.setGeneralEmailBody("test email body");
        notificationRequest.setDocumentContents(new byte[5]);

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_CONSENT_GENERAL_EMAIL_ATTACHMENT.name());

        assertNull(returnedTemplateVars.get("courtName"));
        assertNull(returnedTemplateVars.get("courtEmail"));
        assertEquals("test email body", returnedTemplateVars.get("generalEmailBody"));
        assertNotNull(returnedTemplateVars.get("link_to_file"));
    }

    @Test
    public void shouldBuildTemplateVarsForGeneralEmailAttachmentContested() {
        setConsentedData();
        notificationRequest.setGeneralEmailBody("test email body");
        notificationRequest.setDocumentContents(new byte[5]);

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT.name());

        assertNull(returnedTemplateVars.get("courtName"));
        assertNull(returnedTemplateVars.get("courtEmail"));
        assertEquals("test email body", returnedTemplateVars.get("generalEmailBody"));
        assertNotNull(returnedTemplateVars.get("link_to_file"));
    }

    @Test
    public void shouldBuildTemplateVarsForTransferToLocalCourt() {
        setConsentedData();
        notificationRequest.setCaseReferenceNumber("123456789");
        notificationRequest.setNotificationEmail("TestCourtEmail@Test.com");
        notificationRequest.setGeneralEmailBody("Additional instructions for the court");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_TRANSFER_TO_LOCAL_COURT.name());

        assertEquals("123456789", returnedTemplateVars.get("caseReferenceNumber"));
        assertEquals("TestCourtEmail@Test.com", returnedTemplateVars.get("notificationEmail"));
        assertEquals("Additional instructions for the court", returnedTemplateVars.get("generalEmailBody"));
    }

    @Test
    public void givenRejectGeneralApplicationTemplate_whenPopulateTemplateVars_thenAddRejectionReasonToTemplateVars() {
        setContestedData();
        notificationRequest.setGeneralApplicationRejectionReason("Test Rejection Reason");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_REJECT_GENERAL_APPLICATION.name());

        assertEquals("Test Rejection Reason", returnedTemplateVars.get("generalApplicationRejectionReason"));
    }

    @Test
    public void givenNotRejectGeneralApplication_whenPopulateTemplateVars_thenNotAddRejectionReasonToTemplateVars() {
        setContestedData();
        notificationRequest.setGeneralApplicationRejectionReason("Test Rejection Reason");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_GENERAL_EMAIL.name());

        assertNull(returnedTemplateVars.get("generalApplicationRejectionReason"));
    }

    @Test
    public void shouldBuildTemplateVarsForHearingEmailConsented() {
        setConsentedData();
        notificationRequest.setSelectedCourt("nottingham");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING.name());

        assertEquals("Nottingham FRC", returnedTemplateVars.get("courtName"));
        assertEquals("FRCNottingham@justice.gov.uk", returnedTemplateVars.get("courtEmail"));
    }

    @Test
    public void givenBarristerAccessAddedTemplate_whenPopulateTemplateVars_thenAddBarristerReferenceNumberToTemplateVars() {
        setContestedData();
        notificationRequest.setBarristerReferenceNumber("1234567890");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_BARRISTER_ACCESS_ADDED.name());

        assertEquals("1234567890", returnedTemplateVars.get("BarristerReferenceNumber"));
        assertEquals(PHONE_OPENING_HOURS, returnedTemplateVars.get("phoneOpeningHours"));
    }

    @Test
    public void givenBarristerAccessRemovedTemplate_whenPopulateTemplateVars_thenAddBarristerReferenceNumberToTemplateVars() {
        setContestedData();
        notificationRequest.setBarristerReferenceNumber("1234567890");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_BARRISTER_ACCESS_REMOVED.name());

        assertEquals("1234567890", returnedTemplateVars.get("BarristerReferenceNumber"));
        assertEquals(PHONE_OPENING_HOURS, returnedTemplateVars.get("phoneOpeningHours"));
    }

    @Test
    public void givenIntervenerAddedEmailTemplate_whenPopulateTemplateVars_thenAddIntervenerSolReferenceNumberToTemplateVars() {
        setContestedData();
        notificationRequest.setIntervenerSolicitorReferenceNumber("1234567890");
        notificationRequest.setIntervenerFullName("test name");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_INTERVENER_ADDED_EMAIL.name());

        assertEquals("1234567890", returnedTemplateVars.get("intervenerSolicitorReferenceNumber"));
        assertEquals("test name", returnedTemplateVars.get("intervenerFullName"));
        assertEquals(PHONE_OPENING_HOURS, returnedTemplateVars.get("phoneOpeningHours"));
    }

    @Test
    public void givenIntervenerSolicitorAddedEmailTemplate_whenPopulateTemplateVars_thenAddIntervenerSolReferenceNumberToTemplateVars() {
        setContestedData();
        notificationRequest.setIntervenerSolicitorReferenceNumber("1234567890");
        notificationRequest.setIntervenerSolicitorFirm("test firm");
        notificationRequest.setIntervenerFullName("test name");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_INTERVENER_SOLICITOR_ADDED_EMAIL.name());

        assertEquals("1234567890", returnedTemplateVars.get("intervenerSolicitorReferenceNumber"));
        assertEquals("test name", returnedTemplateVars.get("intervenerFullName"));
        assertEquals("test firm", returnedTemplateVars.get("intervenerSolicitorFirm"));
        assertEquals(PHONE_OPENING_HOURS, returnedTemplateVars.get("phoneOpeningHours"));
    }

    public void givenIntervenerRemovedEmailTemplate_whenPopulateTemplateVars_thenAddIntervenerSolReferenceNumberToTemplateVars() {
        setContestedData();
        notificationRequest.setIntervenerSolicitorReferenceNumber("1234567890");
        notificationRequest.setIntervenerFullName("test name");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_INTERVENER_REMOVED_EMAIL.name());

        assertEquals("1234567890", returnedTemplateVars.get("intervenerSolicitorReferenceNumber"));
        assertEquals("test name", returnedTemplateVars.get("intervenerFullName"));
        assertEquals(PHONE_OPENING_HOURS, returnedTemplateVars.get("phoneOpeningHours"));
    }

    @Test
    public void givenIntervenerSolicitorRemovedEmailTemplate_whenPopulateTemplateVars_thenAddIntervenerSolReferenceNumberToTemplateVars() {
        setContestedData();
        notificationRequest.setIntervenerSolicitorReferenceNumber("1234567890");
        notificationRequest.setIntervenerSolicitorFirm("test firm");
        notificationRequest.setIntervenerFullName("test name");

        Map<String, Object> returnedTemplateVars =

            emailService.buildTemplateVars(notificationRequest, FR_INTERVENER_SOLICITOR_REMOVED_EMAIL.name());

        assertEquals("1234567890", returnedTemplateVars.get("intervenerSolicitorReferenceNumber"));
        assertEquals("test name", returnedTemplateVars.get("intervenerFullName"));
        assertEquals("test firm", returnedTemplateVars.get("intervenerSolicitorFirm"));
        assertEquals(PHONE_OPENING_HOURS, returnedTemplateVars.get("phoneOpeningHours"));
    }

    @Test
    public void givenJudgeReadyToReviewTemplate_whenPopulateTemplateVars_thenAddHearingDateToTemplateVars() {
        setContestedData();
        notificationRequest.setHearingDate("1 January 2024");
        notificationRequest.setNotificationEmail("judge@email.com");

        Map<String, Object> returnedTemplateVars =
            emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_JUDGE.name());

        assertEquals(TEST_CASE_FAMILY_MAN_ID, returnedTemplateVars.get("caseReferenceNumber"));
        assertEquals("judge@email.com", returnedTemplateVars.get("notificationEmail"));
        assertEquals("1 January 2024", returnedTemplateVars.get("hearingDate"));
        assertEquals(APPLICANT_NAME, returnedTemplateVars.get("applicantName"));
        assertEquals(RESPONDENT_NAME, returnedTemplateVars.get("respondentName"));
        assertEquals("http://localhost:3000", returnedTemplateVars.get("manageCaseBaseUrl"));
    }

    @Test
    public void givenAdminReadyToReviewTemplate_whenPopulateTemplateVars_thenAddHearingDateToTemplateVars() {
        setContestedData();
        notificationRequest.setHearingDate("1 January 2024");
        notificationRequest.setNotificationEmail("admin@email.com");

        Map<String, Object> returnedTemplateVars =
            emailService.buildTemplateVars(notificationRequest, FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN.name());

        assertEquals(TEST_CASE_FAMILY_MAN_ID, returnedTemplateVars.get("caseReferenceNumber"));
        assertEquals("admin@email.com", returnedTemplateVars.get("notificationEmail"));
        assertEquals("1 January 2024", returnedTemplateVars.get("hearingDate"));
        assertEquals(APPLICANT_NAME, returnedTemplateVars.get("applicantName"));
        assertEquals(RESPONDENT_NAME, returnedTemplateVars.get("respondentName"));
        assertEquals("http://localhost:3000", returnedTemplateVars.get("manageCaseBaseUrl"));
    }

    @Test
    public void testSendConfirmationEmailForContestedDraftOrderReviewOverdue() throws NotificationClientException {
        NotificationRequest nr = NotificationRequest.builder()
            .notificationEmail("recipient@test.com")
            .caseReferenceNumber("5457543354")
            .caseType(EmailService.CONTESTED)
            .applicantName(APPLICANT_NAME)
            .respondentName(RESPONDENT_NAME)
            .hearingDate("5 January 2024")
            .selectedCourt("liverpool")
            .judgeName("judge@test.com")
            .oldestDraftOrderDate("11 February 2024")
            .build();

        emailService.sendConfirmationEmail(nr, FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE);

        Map<String, Object> expectedTemplateFields = Map.of(
            "caseReferenceNumber", "5457543354",
            "applicantName", APPLICANT_NAME,
            "respondentName", RESPONDENT_NAME,
            "hearingDate", "5 January 2024",
            "courtName", "Liverpool FRC",
            "courtEmail", "FRCLiverpool@Justice.gov.uk",
            "judgeName", "judge@test.com",
            "oldestDraftOrderDate", "11 February 2024"
        );

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE.name())),
            eq("recipient@test.com"),
            templateFieldsArgumentCaptor.capture(),
            anyString(), isNull());

        Map<String, Object> actualTemplateFields = templateFieldsArgumentCaptor.getValue();
        expectedTemplateFields.forEach((k, v) -> assertEquals(v, actualTemplateFields.get(k)));
    }

    @Test
    public void testSendRefusedDraftOrderOrPsa() throws NotificationClientException {
        NotificationRequest nr = NotificationRequest.builder()
            .notificationEmail("recipient@test.com")
            .caseReferenceNumber("5457543354")
            .caseType(EmailService.CONTESTED)
            .applicantName(APPLICANT_NAME)
            .respondentName(RESPONDENT_NAME)
            .selectedCourt("liverpool")
            .documentName("TEST.doc")
            .judgeFeedback("Feedback")
            .hearingDate("30 December 2024")
            .name("Mary")
            .build();

        emailService.sendConfirmationEmail(nr, FR_CONTESTED_DRAFT_ORDER_OR_PSA_REFUSED);

        Map<String, Object> expectedTemplateFields = Map.of(
            "caseReferenceNumber", "5457543354",
            "applicantName", APPLICANT_NAME,
            "respondentName", RESPONDENT_NAME,
            "courtName", "Liverpool FRC",
            "courtEmail", "FRCLiverpool@Justice.gov.uk",
            "documentName", "TEST.doc",
            "judgeFeedback", "Feedback",
            "hearingDate", "30 December 2024",
            "name", "Mary"
        );

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(FR_CONTESTED_DRAFT_ORDER_OR_PSA_REFUSED.name())),
            eq("recipient@test.com"),
            templateFieldsArgumentCaptor.capture(),
            anyString(), isNull());

        Map<String, Object> actualTemplateFields = templateFieldsArgumentCaptor.getValue();
        expectedTemplateFields.forEach((k, v) -> assertEquals(v, actualTemplateFields.get(k)));
    }

    @Test
    public void givenInvalidEmail_whenSendEmail_thenExceptionThrown() throws NotificationClientException {
        doThrow(new NotificationClientException("email_address Not a valid email address"))
            .when(mockClient).sendEmail(any(), any(), any(), any(), any());

        assertThrows(InvalidEmailAddressException.class,
            () -> emailService.sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL));
    }

    @Test
    public void givenSendEmailError_whenSendEmail_thenExceptionThrown() throws NotificationClientException {
        doThrow(new NotificationClientException("Internal Server Error"))
            .when(mockClient).sendEmail(any(), any(), any(), any(), any());

        assertThrows(SendEmailException.class,
            () -> emailService.sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL));
    }

    private void assertContestedTemplateVariablesAreAbsent(Map<String, Object> returnedTemplateVars) {
        assertNull(returnedTemplateVars.get("courtName"));
        assertNull(returnedTemplateVars.get("courtEmail"));
        assertNull(returnedTemplateVars.get("generalEmailBody"));
    }
}
