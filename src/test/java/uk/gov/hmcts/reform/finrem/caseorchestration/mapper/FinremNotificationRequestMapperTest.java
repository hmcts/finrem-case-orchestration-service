package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getDefaultConsentedFinremCaseData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestData.getDefaultContestedFinremCaseData;


@ExtendWith(MockitoExtension.class)
class FinremNotificationRequestMapperTest {

    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-three-collection-no-track.json";
    protected static final String EMPTY_STRING = "";

    @InjectMocks
    FinremNotificationRequestMapper notificationRequestMapper;
    @InjectMocks
    ObjectMapper mapper;
    @Mock
    ConsentedApplicationHelper consentedApplicationHelper;

    private final FinremCaseDetails consentedFinremCaseDetails = getConsentedFinremCaseDetails();
    private final FinremCaseDetails contestedFinremCaseDetails = getContestedFinremCaseDetails();

    @BeforeEach
    public void setup() {
        // Initialize mock objects
        MockitoAnnotations.openMocks(this);

        // Register the JavaTimeModule for Java 8 Date/Time support
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateNotificationRequestForAppSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            consentedFinremCaseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void shouldCreateNotificationRequestForAppSolicitorForConsentedJourneyIsNotDigital() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            consentedFinremCaseDetails, true);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertTrue(notificationRequest.getIsNotDigital());
        assertEquals("consented", notificationRequest.getCaseType());
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void shouldCreateNotificationRequestForRespSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            consentedFinremCaseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void shouldCreateNotificationRequestFoRespSolicitorForConsentedJourneyIsNotDigital() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            consentedFinremCaseDetails, true);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertTrue(notificationRequest.getIsNotDigital());
        assertEquals("consented", notificationRequest.getCaseType());
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void shouldReturnEmptyStringForSolicitorReferenceWhenNotProvided() {
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails();
        caseDetails.getData().getContactDetailsWrapper().setSolicitorReference(null);
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            caseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(EMPTY_STRING, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void shouldReturnHearingTypeForPrepareForHearingContestedEventInvoke() {
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails();
        caseDetails.getData().setHearingType(HearingTypeDirection.FDA);
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            caseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
        assertEquals("First Directions Appointment (FDA)", notificationRequest.getHearingType());
    }

    @Test
    void shouldReturnHearingTypeForPrepareForHearingContestedEventInvokeIntervener() {
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails();
        caseDetails.getData().setHearingType(HearingTypeDirection.FDA);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(TEST_SOLICITOR_EMAIL)
            .solicitorNameKey(TEST_SOLICITOR_NAME)
            .solicitorReferenceKey("RG-123456789").build();
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(
            caseDetails, dataKeysWrapper);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals("RG-123456789", notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
        assertEquals("First Directions Appointment (FDA)", notificationRequest.getHearingType());
    }

    @Test
    void shouldCreateNotificationRequestForAppSolicitorForContestedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            contestedFinremCaseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void givenApplicantSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCaseData caseData = getDefaultContestedFinremCaseData();
        caseData.setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails(caseData);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails);

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    void givenRespondentSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCaseData caseData = getDefaultContestedFinremCaseData();
        caseData.setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Respondent", TEST_RESP_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL));
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails(caseData);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails);

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    void givenApplicantSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCaseData caseData = getDefaultConsentedFinremCaseData();
        caseData.setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails(caseData);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            caseDetails);

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
    }

    @Test
    void givenRespondentSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCaseData caseData = getDefaultConsentedFinremCaseData();
        caseData.setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Respondent", TEST_RESP_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL));
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails(caseData);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails);

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
    }

    @Test
    void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearing() {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(TEST_JSON, mapper);
        FinremCaseData caseData = caseDetails.getData();

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(
            caseData.getInterimWrapper().getInterimHearingsScreenField()).orElse(Collections.emptyList());

        assertThat(interimHearingList.isEmpty(), is(false));
    }

    @Test
    void shouldCreateNotificationRequestForIntervenerNotification() {
        Organisation org = Organisation.builder().organisationName("test org").organisationID("1").build();
        OrganisationPolicy intervenerOrg = OrganisationPolicy.builder().organisation(org).build();
        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerName("intervener name")
            .intervenerOrganisation(intervenerOrg)
            .intervenerSolicitorReference(TEST_SOLICITOR_REFERENCE).build();
        NotificationRequest notificationRequest = notificationRequestMapper.buildNotificationRequest(
            contestedFinremCaseDetails, intervenerDetails, TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
        assertEquals("intervener name", notificationRequest.getIntervenerFullName());
        assertEquals("test org", notificationRequest.getIntervenerSolicitorFirm());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getIntervenerSolicitorReferenceNumber());
    }

    @Test
    void shouldCreateNotificationRequestForBarristerNotification() {
        Organisation org = Organisation.builder().organisationName("test org").organisationID("1").build();
        NotificationRequest notificationRequest = notificationRequestMapper.buildNotificationRequest(
            contestedFinremCaseDetails, Barrister.builder()
                .name("barrister name")
                .email("barrister@email.com")
                .organisation(org)
                .build());

        assertEquals("barrister name", notificationRequest.getName());
        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals("1", notificationRequest.getBarristerReferenceNumber());
        assertEquals("barrister@email.com", notificationRequest.getNotificationEmail());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals(CTSC_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
    }

    @SneakyThrows
    private List<RepresentationUpdateHistoryCollection> getChangeOfRepresentationListJson(String party,
                                                                                          String latestSolicitorName,
                                                                                          String latestSolicitorEmail) {
        return List.of(
            RepresentationUpdateHistoryCollection.builder()
                .value(RepresentationUpdate.builder()
                    .party(party)
                    .clientName("TestClient Name")
                    .via("Notice of Change")
                    .by("TestSolicitor2 Name")
                    .date(LocalDateTime.now().minusDays(5))
                    .added(ChangedRepresentative.builder()
                        .email("testSolicitor2@test.com")
                        .name("TestSolicitor2 Name")
                        .organisation(Organisation.builder().build())
                        .build())
                    .removed(ChangedRepresentative.builder()
                        .email("testSolicitor1@test.com")
                        .name("TestSolicitor1 Name")
                        .organisation(Organisation.builder().build())
                        .build())
                    .build()).build(),
            RepresentationUpdateHistoryCollection.builder()
                .value(RepresentationUpdate.builder()
                    .party(party)
                    .via("Notice of Change")
                    .by(latestSolicitorName)
                    .date(LocalDateTime.now())
                    .added(ChangedRepresentative.builder()
                        .email(latestSolicitorEmail)
                        .name(latestSolicitorName)
                        .organisation(Organisation.builder().build())
                        .build())
                    .removed(ChangedRepresentative.builder()
                        .email("testSolicitor2@test.com")
                        .name("TestSolicitor2 Name")
                        .organisation(Organisation.builder().build())
                        .build())
                    .build())
                .build());
    }
}
