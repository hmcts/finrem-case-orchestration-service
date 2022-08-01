package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Organisation;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdateHistoryCollection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class NotificationRequestMapperTest extends BaseServiceTest {

    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";

    protected static final String EMPTY_STRING = "";
    @Autowired
    NotificationRequestMapper notificationRequestMapper;

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourney() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            getConsentedCallbackRequest().getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldReturnEmptyStringForSolicitorReferenceWhenNotProvided() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            getContestedCallbackRequestWithCaseDataValues(Collections.singletonMap(SOLICITOR_REFERENCE, null)).getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(EMPTY_STRING, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourney() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
    }

    @Test
    public void givenApplicantSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest callbackRequest = getContestedNewCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData()
            .setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    public void givenRespondentSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest callbackRequest = getContestedNewCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().setRepresentationUpdateHistory(getChangeOfRepresentationListJson(
            "Respondent", TEST_RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    public void givenApplicantSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest callbackRequest = getConsentedNewCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().setRepresentationUpdateHistory(getChangeOfRepresentationListJson(
            "Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
    }

    @Test
    public void givenRespondentSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest callbackRequest = getConsentedNewCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().setRepresentationUpdateHistory(getChangeOfRepresentationListJson(
            "Respondent", TEST_RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourneyForInterimHearing() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_JSON), mapper);
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(
            caseData.getInterimWrapper().getInterimHearings()).orElse(Collections.emptyList());

        interimHearingList.forEach(item -> verifyAppData(caseDetails, item));
    }

    private void verifyAppData(FinremCaseDetails caseDetails, InterimHearingCollection item) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            caseDetails, item);
        System.out.println(notificationRequest);

        assertEquals("123", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertThat("checking in loop", notificationRequest.getSelectedCourt(),
            anyOf(is("Gloucester and Cheltenham County and Family Court"),
                is("Croydon County Court And Family Court")));
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearing() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(TEST_JSON), mapper);
        FinremCaseData caseData = caseDetails.getCaseData();

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(
            caseData.getInterimWrapper().getInterimHearings()).orElse(Collections.emptyList());

        interimHearingList.forEach(data -> verifyData(caseDetails, data));
    }

    private void verifyData(FinremCaseDetails caseDetails, InterimHearingCollection data) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            caseDetails, data);

        assertEquals("123", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertThat("checking in loop", notificationRequest.getSelectedCourt(),
            anyOf(is("Gloucester and Cheltenham County and Family Court"),
            is("Croydon County Court And Family Court")));
    }


    @SneakyThrows
    private List<RepresentationUpdateHistoryCollection> getChangeOfRepresentationListJson(String party,
                                                                                          String latestSolicitorName,
                                                                                          String latestSolicitorEmail) {
        return List.of(
            RepresentationUpdateHistoryCollection.builder()
                .value(RepresentationUpdate.builder()
                    .party(party)
                    .name("TestClient Name")
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
