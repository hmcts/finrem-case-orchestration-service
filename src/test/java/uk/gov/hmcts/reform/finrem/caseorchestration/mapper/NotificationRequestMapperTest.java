package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFERRED_DETAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

public class NotificationRequestMapperTest extends BaseServiceTest {

    protected static final String EMPTY_STRING = "";
    private static final String INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing-two-collection.json";
    private static final String CONSENTED_HEARING_JSON = "/fixtures/consented.listOfHearing/list-for-hearing-notification.json";

    @Autowired
    NotificationRequestMapper notificationRequestMapper;
    @Autowired
    ConsentedHearingHelper helper;

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
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourneyWithNatureIsVariationOrder() {
        CallbackRequest callbackRequest = getConsentedCallbackRequestForVariationOrder();
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
        assertEquals("variation", notificationRequest.getCaseOrderType());
        assertEquals("Variation", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
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
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForContestedJourneyForInterimHearing() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(INTERIM_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).toList();

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();

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

        assertEquals("respondent test", notificationRequest.getRespondentName());
        assertEquals("Applicant test", notificationRequest.getApplicantName());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearing() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(INTERIM_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).toList();

        List<Map<String, Object>> interimDataMap = interimHearingItems.stream()
                .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
                })).toList();
        interimDataMap.forEach(data -> verifyData(callbackRequest, data));
    }

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourneyForHearing() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(CONSENTED_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<ConsentedHearingDataElement> elements = hearings.stream().map(ConsentedHearingDataWrapper::getValue).toList();

        List<Map<String, Object>> hearingDataMap = elements.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();

        hearingDataMap.forEach(data -> verifyConsentedAppHearingData(callbackRequest, data));
    }

    private void verifyConsentedAppHearingData(CallbackRequest callbackRequest, Map<String, Object> data) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForConsentApplicantSolicitor(
            callbackRequest.getCaseDetails(), data);

        assertEquals("12345678", notificationRequest.getCaseReferenceNumber());
        assertEquals("1111-2222-3333", notificationRequest.getSolicitorReferenceNumber());
        assertEquals("EM18D84321", notificationRequest.getDivorceCaseNumber());
        assertEquals("Solicitor Name", notificationRequest.getName());
        assertEquals("solicitor@mailinator.com", notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
        assertThat("checking in loop", notificationRequest.getSelectedCourt(),
            anyOf(is("liverpool"),
                is("cfc")));

        assertEquals("Lee Powers Mcbride", notificationRequest.getRespondentName());
        assertEquals("Austin Bates Porter", notificationRequest.getApplicantName());
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourneyForHearing() {
        CallbackRequest callbackRequest = buildHearingCallbackRequest(CONSENTED_HEARING_JSON);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.put(RESP_SOLICITOR_REFERENCE,TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put("divorceCaseNumber",TEST_DIVORCE_CASE_NUMBER);
        caseData.put("rSolicitorEmail", TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<ConsentedHearingDataElement> elements = hearings.stream().map(ConsentedHearingDataWrapper::getValue).toList();

        List<Map<String, Object>> hearingDataMap = elements.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();
        hearingDataMap.forEach(data -> verifyConsentedRespHearingData(callbackRequest, data));
    }

    private void verifyConsentedRespHearingData(CallbackRequest callbackRequest, Map<String, Object> data) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails(), data);

        assertEquals("12345678", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
        assertThat("checking in loop", notificationRequest.getSelectedCourt(),
            anyOf(is("liverpool"),
                is("cfc")));
        assertEquals("Lee Powers Mcbride", notificationRequest.getRespondentName());
        assertEquals("Austin Bates Porter", notificationRequest.getApplicantName());
    }

    @Test
    public void givenContestedCase_whenReferToJudgeInvoked_thenEmailBodyContainsDetails() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_REFERRED_DETAIL,
            "Application 1 - Received From - Applicant");
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails());

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("Application 1 - Received From - Applicant", notificationRequest.getGeneralEmailBody());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
    }

    @Test
    public void givenValidData_createBarristerNotificationRequest() {
        CaseDetails caseDetails = buildCaseDetails();
        Barrister barrister = createBarrister();
        NotificationRequest result = notificationRequestMapper.buildNotificationRequest(caseDetails, barrister);
        assertEquals("1234", result.getBarristerReferenceNumber());
    }


    private Barrister createBarrister() {
        Organisation organisation = Organisation.builder()
            .organisationID("1234")
            .organisationName("Org Name")
            .build();
        Barrister barrister = Barrister.builder()
            .name("barrister")
            .email("barrister@barrister.com")
            .organisation(organisation)
            .phone("0123456789")
            .build();
        return barrister;
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
        assertEquals("respondent test", notificationRequest.getRespondentName());
        assertEquals("Applicant test", notificationRequest.getApplicantName());
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
