package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

public class NotificationRequestMapperTest extends BaseServiceTest {

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
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(REPRESENTATION_UPDATE_HISTORY,
            getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    public void givenRespondentSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(REPRESENTATION_UPDATE_HISTORY,
            getChangeOfRepresentationListJson("Respondent", TEST_RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("contested"));
    }

    @Test
    public void givenApplicantSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(REPRESENTATION_UPDATE_HISTORY,
            getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
    }

    @Test
    public void givenRespondentSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        CallbackRequest callbackRequest = getConsentedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(REPRESENTATION_UPDATE_HISTORY,
            getChangeOfRepresentationListJson("Respondent", TEST_RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_EMAIL));

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            callbackRequest.getCaseDetails());

        assertThat(notificationRequest.getNotificationEmail(), is(TEST_RESP_SOLICITOR_EMAIL));
        assertThat(notificationRequest.getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(notificationRequest.getCaseType(), is("consented"));
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourneyForInterimHearing() {
        CallbackRequest callbackRequest = buildInterimHearingCallbackRequest();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        List<InterimHearingItems> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).collect(Collectors.toList());

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).collect(Collectors.toList());

        interimDataMap.forEach(data -> verifyAppData(callbackRequest, data));
    }

    private void verifyAppData(CallbackRequest callbackRequest, Map<String, Object> data) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            callbackRequest.getCaseDetails(), data);

        assertEquals("123", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertThat("checking in loop", notificationRequest.getSelectedCourt(),
            anyOf(is("bristol"),
                is("cfc")));
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearing() {
        CallbackRequest callbackRequest = buildInterimHearingCallbackRequest();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        List<InterimHearingItems> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).collect(Collectors.toList());

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
                .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
                })).collect(Collectors.toList());
        interimDataMap.forEach(data -> verifyData(callbackRequest, data));
    }

    private void verifyData(CallbackRequest callbackRequest, Map<String, Object> data) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails(), data);

        assertEquals("123", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        assertThat("checking in loop", notificationRequest.getSelectedCourt(),
            anyOf(is("bristol"),
            is("cfc")));
    }


    @SneakyThrows
    private List<Element<RepresentationUpdate>> getChangeOfRepresentationListJson(String party,
                                                                                  String latestSolicitorName,
                                                                                  String latestSolicitorEmail) {
        return Stream.of(
            element(UUID.randomUUID(), RepresentationUpdate.builder()
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
                .build()),
            element(UUID.randomUUID(), RepresentationUpdate.builder()
                .party(party)
                .clientName("TestClient Name")
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
        ).collect(Collectors.toList());
    }


}
