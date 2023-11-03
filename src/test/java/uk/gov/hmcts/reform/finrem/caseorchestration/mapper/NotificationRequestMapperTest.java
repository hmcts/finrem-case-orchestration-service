package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
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
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
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
    public void shouldReturnHearingTypeForPrepareForHearingContestedEventInvoke() {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForApplicantSolicitor(
            getContestedCallbackRequestWithCaseDataValues(Collections.singletonMap(HEARING_TYPE, "First Directions Appointment (FDA)"))
                .getCaseDetails());

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
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourneyWithNatureIsVariationOrderFinremPojo() {
        FinremCallbackRequest callbackRequest = getConsentedFinremCallbackRequestForVariationOrder();
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails(), new HashMap<>());

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
    public void shouldCreateNotificationRequestForIntervenerSolicitorForContestedJourney() {
        CallbackRequest callbackRequest = getContestedCallbackRequest();
        callbackRequest.getCaseDetails().getData().put("intervener1SolEmail", TEST_RESP_SOLICITOR_EMAIL);
        callbackRequest.getCaseDetails().getData().put("intervener1SolName", TEST_RESP_SOLICITOR_NAME);
        callbackRequest.getCaseDetails().getData().put("intervener1SolicitorReference", TEST_RESP_SOLICITOR_REFERENCE);
        SolicitorCaseDataKeysWrapper dataKeysWrapper = SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(TEST_RESP_SOLICITOR_EMAIL)
            .solicitorNameKey(TEST_RESP_SOLICITOR_NAME)
            .solicitorReferenceKey(TEST_RESP_SOLICITOR_REFERENCE).build();
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForIntervenerSolicitor(
            callbackRequest.getCaseDetails(), dataKeysWrapper);

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

        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals("contested", notificationRequest.getCaseType());
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

        assertEquals("testRespSolicitor@email.com", notificationRequest.getNotificationEmail());
        assertEquals("Test Resp Sol", notificationRequest.getName());
        assertEquals("contested", notificationRequest.getCaseType());
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

        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals("consented", notificationRequest.getCaseType());
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

        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals("consented", notificationRequest.getCaseType());
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
        if (notificationRequest.getSelectedCourt().equals("bristol")) {
            assertTrue(notificationRequest.getSelectedCourt().contains("bristol"));
        }
        if (notificationRequest.getSelectedCourt().equals("cfc")) {
            assertTrue(notificationRequest.getSelectedCourt().contains("cfc"));
        }
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
    public void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearingFinremData() throws Exception {
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest = buildFinremCallbackRequest(INTERIM_HEARING_JSON);
        FinremCaseDataContested caseData = callbackRequest.getCaseDetails().getData();

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
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

    @Test
    public void shouldCreateNotificationRequestForAppSolicitorForConsentedJourneyForHearingFinremCaseData() {
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest = buildHearingFinremCallbackRequest(CONSENTED_HEARING_JSON);
        FinremCaseDataConsented caseData = callbackRequest.getCaseDetails().getData();
        caseData.setCcdCaseType(CaseType.CONSENTED);

        List<ConsentedHearingDataWrapper> hearings = caseData.getListForHearings();
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

    private void verifyConsentedAppHearingData(FinremCallbackRequest callbackRequest, Map<String, Object> data) {
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
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put("divorceCaseNumber", TEST_DIVORCE_CASE_NUMBER);
        caseData.put("rSolicitorEmail", TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseData);
        List<ConsentedHearingDataElement> elements = hearings.stream().map(ConsentedHearingDataWrapper::getValue).toList();

        List<Map<String, Object>> hearingDataMap = elements.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();
        hearingDataMap.forEach(data -> verifyConsentedRespHearingData(callbackRequest, data));
    }

    @Test
    public void shouldCreateNotificationRequestForRespSolicitorForConsentedJourneyForHearingFinremCaseData() throws Exception {
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest = buildFinremCallbackRequest(CONSENTED_HEARING_JSON);

        FinremCaseDataConsented caseData = callbackRequest.getCaseDetails().getData();
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);

        List<ConsentedHearingDataWrapper> hearings = caseData.getListForHearings();
        List<ConsentedHearingDataElement> elements = hearings.stream().map(ConsentedHearingDataWrapper::getValue).toList();

        List<Map<String, Object>> hearingDataMap = elements.stream()
            .map(obj -> new ObjectMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();
        hearingDataMap.forEach(data -> verifyConsentedRespHearingData(callbackRequest, data));
    }

    private void verifyConsentedRespHearingData(FinremCallbackRequest callbackRequest, Map<String, Object> data) {
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
    public void givenValidData_createBarristerNotificationRequest() {
        CaseDetails caseDetails = buildCaseDetails();
        Barrister barrister = createBarrister();
        NotificationRequest result = notificationRequestMapper.buildInterimHearingNotificationRequest(caseDetails, barrister);
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

    private void verifyData(FinremCallbackRequest callbackRequest, Map<String, Object> data) {
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForRespondentSolicitor(
            callbackRequest.getCaseDetails(), data);

        assertEquals("123", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_RESP_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_RESP_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_RESP_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("contested", notificationRequest.getCaseType());
        if (notificationRequest.getSelectedCourt().equals("bristol")) {
            assertTrue(notificationRequest.getSelectedCourt().contains("bristol"));
        }
        if (notificationRequest.getSelectedCourt().equals("cfc")) {
            assertTrue(notificationRequest.getSelectedCourt().contains("cfc"));
        }
        assertEquals("respondent test", notificationRequest.getRespondentName());
        assertEquals("Applicant test", notificationRequest.getApplicantName());
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
        if (notificationRequest.getSelectedCourt().equals("bristol")) {
            assertTrue(notificationRequest.getSelectedCourt().contains("bristol"));
        }
        if (notificationRequest.getSelectedCourt().equals("cfc")) {
            assertTrue(notificationRequest.getSelectedCourt().contains("cfc"));
        }
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
