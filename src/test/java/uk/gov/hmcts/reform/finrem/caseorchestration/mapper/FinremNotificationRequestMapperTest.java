package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;

public class FinremNotificationRequestMapperTest extends BaseServiceTest {

    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";
    protected static final String EMPTY_STRING = "";

    @Autowired
    FinremNotificationRequestMapper notificationRequestMapper;

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            getConsentedNewCallbackRequest().getCaseDetails());

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
    public void shouldReturnEmptyStringForSolicitorReferenceWhenNotProvided() {
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();
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
    public void shouldReturnHearingTypeForPrepareForHearingContestedEventInvoke() {
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();
        caseDetails.getData().setHearingType(HearingTypeDirection.FDA);
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            caseDetails);

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
    public void shouldReturnHearingTypeForPrepareForHearingContestedEventInvokeIntervener() {
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();
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
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourney() {
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            caseDetails);

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
    public void givenApplicantSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        callbackRequest.getCaseDetails().getData()
            .setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    public void givenRespondentSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        callbackRequest.getCaseDetails().getData().setRepresentationUpdateHistory(getChangeOfRepresentationListJson(
            "Respondent", TEST_RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    public void givenApplicantSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCallbackRequest callbackRequest = getConsentedNewCallbackRequest();
        callbackRequest.getCaseDetails().getData().setRepresentationUpdateHistory(getChangeOfRepresentationListJson(
            "Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
    }

    @Test
    public void givenRespondentSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCallbackRequest callbackRequest = getConsentedNewCallbackRequest();
        callbackRequest.getCaseDetails().getData().setRepresentationUpdateHistory(getChangeOfRepresentationListJson(
            "Respondent", TEST_RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearing() {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(TEST_JSON, mapper);
        FinremCaseData caseData = caseDetails.getData();

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(
            caseData.getInterimWrapper().getInterimHearings()).orElse(Collections.emptyList());

        assertThat(interimHearingList.isEmpty(), is(false));

    }

    @Test
    public void shouldCreateNotificationRequestForIntervenerNotification() {
        Organisation org = Organisation.builder().organisationName("test org").organisationID("1").build();
        OrganisationPolicy intervenerOrg = OrganisationPolicy.builder().organisation(org).build();
        IntervenerOneWrapper intervenerDetails = IntervenerOneWrapper.builder()
            .intervenerName("intervener name")
            .intervenerOrganisation(intervenerOrg)
            .intervenerSolicitorReference(TEST_SOLICITOR_REFERENCE).build();
        String recipient = TEST_SOLICITOR_NAME;
        String email = TEST_SOLICITOR_EMAIL;
        String referenceNumber = TEST_SOLICITOR_REFERENCE;
        FinremCaseDetails caseDetails = getContestedNewCallbackRequest().getCaseDetails();
        NotificationRequest notificationRequest = notificationRequestMapper.buildNotificationRequest(
            caseDetails, intervenerDetails, recipient, email, referenceNumber);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
        assertEquals("intervener name", notificationRequest.getIntervenerFullName());
        assertEquals("test org", notificationRequest.getIntervenerSolicitorFirm());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getIntervenerSolicitorReferenceNumber());
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
