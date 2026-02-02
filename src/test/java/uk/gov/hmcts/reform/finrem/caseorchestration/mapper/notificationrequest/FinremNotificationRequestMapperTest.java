package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_APP_BARRISTER_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_APP_BARRISTER_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_APP_BARRISTER_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV1_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV1_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV1_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV1_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_BARRISTER_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_BARRISTER_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_BARRISTER_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_INTV_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_BARRISTER_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_BARRISTER_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_BARRISTER_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.barrister;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.barristers;
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
    @Mock
    NotificationRequestBuilderFactory builderFactory;

    private CourtDetailsConfiguration courtDetailsConfiguration;
    private FinremCaseDetails consentedFinremCaseDetails;
    private FinremCaseDetails contestedFinremCaseDetails;

    @BeforeEach
    void setup() {
        // Register the JavaTimeModule for Java 8 Date/Time support
        mapper.registerModule(new JavaTimeModule());

        courtDetailsConfiguration = mock(CourtDetailsConfiguration.class);
        consentedFinremCaseDetails = getConsentedFinremCaseDetails();
        contestedFinremCaseDetails = getContestedFinremCaseDetails();

        mockNotificationRequestBuilderFactory();
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
        caseDetails.getData().getListForHearingWrapper().setHearingType(HearingTypeDirection.FDA);
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
        caseDetails.getData().getListForHearingWrapper().setHearingType(HearingTypeDirection.FDA);
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

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(notificationRequest.getName()).isEqualTo(TEST_SOLICITOR_NAME);
        assertThat(notificationRequest.getCaseType()).isEqualTo("contested");
    }

    @Test
    void givenRespondentSolicitorNoticeOfChangeOnContestedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCaseData caseData = getDefaultContestedFinremCaseData();
        caseData.setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Respondent", TEST_RESP_SOLICITOR_NAME,
            TEST_RESP_SOLICITOR_EMAIL));
        FinremCaseDetails caseDetails = getContestedFinremCaseDetails(caseData);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails);

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo(TEST_RESP_SOLICITOR_EMAIL);
        assertThat(notificationRequest.getName()).isEqualTo(TEST_RESP_SOLICITOR_NAME);
        assertThat(notificationRequest.getCaseType()).isEqualTo("contested");
    }

    @Test
    void givenApplicantSolicitorNoticeOfChangeOnConsentedWhenGetNotificationRequestCalledThenReturnNotificationRequestToAddedSolicitor() {
        FinremCaseData caseData = getDefaultConsentedFinremCaseData();
        caseData.setRepresentationUpdateHistory(getChangeOfRepresentationListJson("Applicant", TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL));
        FinremCaseDetails caseDetails = getConsentedFinremCaseDetails(caseData);

        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForNoticeOfChange(
            caseDetails);

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(notificationRequest.getName()).isEqualTo(TEST_SOLICITOR_NAME);
        assertThat(notificationRequest.getCaseType()).isEqualTo("consented");
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

        assertThat(notificationRequest.getNotificationEmail()).isEqualTo(TEST_RESP_SOLICITOR_EMAIL);
        assertThat(notificationRequest.getName()).isEqualTo(TEST_RESP_SOLICITOR_NAME);
        assertThat(notificationRequest.getCaseType()).isEqualTo("consented");
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
    }

    @Test
    void shouldCreateNotificationRequestForRespSolicitorForContestedJourneyForInterimHearing() {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(TEST_JSON, mapper);
        FinremCaseData caseData = caseDetails.getData();

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(
            caseData.getInterimWrapper().getInterimHearingsScreenField()).orElse(Collections.emptyList());

        assertThat(interimHearingList.isEmpty()).isEqualTo(false);
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

    @ParameterizedTest
    @MethodSource("intervenerOrganisationScenarios")
    void givenOrgIsNotPresent_whenIntervenerDetailsProvided_thenCreateNotificationRequest(
        OrganisationPolicy organisationPolicy, String expectedFirmName) {

        IntervenerOne intervenerDetails = IntervenerOne.builder()
            .intervenerName("intervener name")
            .intervenerOrganisation(organisationPolicy)
            .intervenerSolicitorReference(TEST_SOLICITOR_REFERENCE)
            .build();

        NotificationRequest notificationRequest = notificationRequestMapper.buildNotificationRequest(
            contestedFinremCaseDetails, intervenerDetails, TEST_SOLICITOR_NAME, TEST_SOLICITOR_EMAIL, TEST_SOLICITOR_REFERENCE);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(TEST_SOLICITOR_EMAIL, notificationRequest.getNotificationEmail());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
        assertEquals("intervener name", notificationRequest.getIntervenerFullName());
        assertEquals(expectedFirmName, notificationRequest.getIntervenerSolicitorFirm());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getIntervenerSolicitorReferenceNumber());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "CONTESTED", "CONSENTED"
    })
    void givenSomeNullProperties_whenGetNotificationRequestForPartyInvoked_thenStandardPropertiesPopulated(
        CaseType caseType) {

        HearingTypeDirection mockedHearingTypeDirection = mock(HearingTypeDirection.class);
        when(mockedHearingTypeDirection.getId()).thenReturn(TEST_HEARING_TYPE);
        FinremCaseData finremCaseData = spiedFinremCaseData(mockedHearingTypeDirection);

        when(finremCaseData.getFullApplicantName()).thenReturn("Full Applicant Name");
        if (CaseType.CONTESTED.equals(caseType)) {
            when(finremCaseData.getFullRespondentNameContested()).thenReturn("Full Respondent Name (CT)");
        } else {
            when(finremCaseData.getFullRespondentNameConsented()).thenReturn("Full Respondent Name (CS)");
        }
        when(finremCaseData.isConsentedApplication()).thenReturn(CaseType.CONSENTED.equals(caseType));
        when(finremCaseData.isContestedApplication()).thenReturn(CaseType.CONTESTED.equals(caseType));
        when(finremCaseData.getRespondentSolicitorName()).thenReturn(TEST_RESP_SOLICITOR_NAME);
        when(finremCaseData.getAppSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseData.getAppSolicitorName()).thenReturn(TEST_SOLICITOR_NAME);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        when(caseDetails.getCaseType()).thenReturn(caseType);
        when(caseDetails.getData()).thenReturn(finremCaseData);

        if (CaseType.CONSENTED.equals(caseType)) {
            when(consentedApplicationHelper.isVariationOrder(finremCaseData)).thenReturn(true);
        }

        finremCaseData.setDivorceCaseNumber(null);
        finremCaseData.getGeneralApplicationWrapper().setGeneralApplicationRejectReason(null);
        finremCaseData.getGeneralEmailWrapper().setGeneralEmailBody(null);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorReference(null);
        finremCaseData.getContactDetailsWrapper().setSolicitorReference(null);
        finremCaseData.getIntervenerOne().setIntervenerSolicitorReference(null);

        NotificationRequest respondentSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails);
        verifyEmptyStringInNotificationRequest(respondentSolicitorNotificationRequest);

        NotificationRequest nonDigitalRespondentSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, false);
        verifyEmptyStringInNotificationRequest(nonDigitalRespondentSolicitorNotificationRequest);

        NotificationRequest digitalRespondentSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, true);
        verifyEmptyStringInNotificationRequest(digitalRespondentSolicitorNotificationRequest);

        NotificationRequest applicantSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails);
        verifyEmptyStringInNotificationRequest(applicantSolicitorNotificationRequest);

        NotificationRequest nonDigitalApplicantSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, false);
        verifyEmptyStringInNotificationRequest(nonDigitalApplicantSolicitorNotificationRequest);

        NotificationRequest isDigitalApplicantSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, true);
        verifyEmptyStringInNotificationRequest(isDigitalApplicantSolicitorNotificationRequest);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "CONTESTED", "CONSENTED"
    })
    void givenCase_whenGetNotificationRequestForPartyInvoked_thenStandardPropertiesPopulated(
        CaseType caseType) {

        HearingTypeDirection mockedHearingTypeDirection = mock(HearingTypeDirection.class);
        when(mockedHearingTypeDirection.getId()).thenReturn(TEST_HEARING_TYPE);
        FinremCaseData finremCaseData = spiedFinremCaseData(mockedHearingTypeDirection);
        when(finremCaseData.getFullApplicantName()).thenReturn("Full Applicant Name");
        if (CaseType.CONTESTED.equals(caseType)) {
            when(finremCaseData.getFullRespondentNameContested()).thenReturn("Full Respondent Name (CT)");
        } else {
            when(finremCaseData.getFullRespondentNameConsented()).thenReturn("Full Respondent Name (CS)");
        }
        when(finremCaseData.isConsentedApplication()).thenReturn(CaseType.CONSENTED.equals(caseType));
        when(finremCaseData.isContestedApplication()).thenReturn(CaseType.CONTESTED.equals(caseType));
        when(finremCaseData.getRespondentSolicitorName()).thenReturn(TEST_RESP_SOLICITOR_NAME);
        when(finremCaseData.getAppSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseData.getAppSolicitorName()).thenReturn(TEST_SOLICITOR_NAME);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getCaseIdAsString()).thenReturn(CASE_ID);
        when(caseDetails.getCaseType()).thenReturn(caseType);
        when(caseDetails.getData()).thenReturn(finremCaseData);

        if (CaseType.CONSENTED.equals(caseType)) {
            when(consentedApplicationHelper.isVariationOrder(finremCaseData)).thenReturn(true);
        }

        NotificationRequest respondentSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails);
        verifyStandardNotificationRequest(caseType, respondentSolicitorNotificationRequest);
        assertThat(respondentSolicitorNotificationRequest.getName()).isEqualTo(TEST_RESP_SOLICITOR_NAME);
        assertThat(respondentSolicitorNotificationRequest.getSolicitorReferenceNumber()).isEqualTo(TEST_RESP_SOLICITOR_REFERENCE);
        assertThat(respondentSolicitorNotificationRequest.getNotificationEmail()).isEqualTo(TEST_RESP_SOLICITOR_EMAIL);
        assertThat(respondentSolicitorNotificationRequest.getIsNotDigital()).isNull();

        NotificationRequest nonDigitalRespondentSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, false);
        verifyStandardNotificationRequest(caseType, nonDigitalRespondentSolicitorNotificationRequest);
        assertThat(nonDigitalRespondentSolicitorNotificationRequest.getName()).isEqualTo(TEST_RESP_SOLICITOR_NAME);
        assertThat(nonDigitalRespondentSolicitorNotificationRequest.getSolicitorReferenceNumber()).isEqualTo(TEST_RESP_SOLICITOR_REFERENCE);
        assertThat(nonDigitalRespondentSolicitorNotificationRequest.getNotificationEmail()).isEqualTo(TEST_RESP_SOLICITOR_EMAIL);
        assertThat(nonDigitalRespondentSolicitorNotificationRequest.getIsNotDigital()).isFalse();

        NotificationRequest digitalRespondentSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, true);
        verifyStandardNotificationRequest(caseType, digitalRespondentSolicitorNotificationRequest);
        assertThat(digitalRespondentSolicitorNotificationRequest.getName()).isEqualTo(TEST_RESP_SOLICITOR_NAME);
        assertThat(digitalRespondentSolicitorNotificationRequest.getSolicitorReferenceNumber()).isEqualTo(TEST_RESP_SOLICITOR_REFERENCE);
        assertThat(digitalRespondentSolicitorNotificationRequest.getNotificationEmail()).isEqualTo(TEST_RESP_SOLICITOR_EMAIL);
        assertThat(digitalRespondentSolicitorNotificationRequest.getIsNotDigital()).isTrue();

        NotificationRequest applicantSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails);
        verifyStandardNotificationRequest(caseType, applicantSolicitorNotificationRequest);
        assertThat(applicantSolicitorNotificationRequest.getName()).isEqualTo(TEST_SOLICITOR_NAME);
        assertThat(applicantSolicitorNotificationRequest.getSolicitorReferenceNumber()).isEqualTo(TEST_SOLICITOR_REFERENCE);
        assertThat(applicantSolicitorNotificationRequest.getNotificationEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(applicantSolicitorNotificationRequest.getIsNotDigital()).isNull();

        NotificationRequest nonDigitalApplicantSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, false);
        verifyStandardNotificationRequest(caseType, nonDigitalApplicantSolicitorNotificationRequest);
        assertThat(nonDigitalApplicantSolicitorNotificationRequest.getName()).isEqualTo(TEST_SOLICITOR_NAME);
        assertThat(nonDigitalApplicantSolicitorNotificationRequest.getSolicitorReferenceNumber()).isEqualTo(TEST_SOLICITOR_REFERENCE);
        assertThat(nonDigitalApplicantSolicitorNotificationRequest.getNotificationEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(nonDigitalApplicantSolicitorNotificationRequest.getIsNotDigital()).isFalse();

        NotificationRequest isDigitalApplicantSolicitorNotificationRequest = notificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, true);
        verifyStandardNotificationRequest(caseType, isDigitalApplicantSolicitorNotificationRequest);
        assertThat(isDigitalApplicantSolicitorNotificationRequest.getName()).isEqualTo(TEST_SOLICITOR_NAME);
        assertThat(isDigitalApplicantSolicitorNotificationRequest.getSolicitorReferenceNumber()).isEqualTo(TEST_SOLICITOR_REFERENCE);
        assertThat(isDigitalApplicantSolicitorNotificationRequest.getNotificationEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(isDigitalApplicantSolicitorNotificationRequest.getIsNotDigital()).isTrue();
    }

    private FinremCaseData spiedFinremCaseData(HearingTypeDirection mockedHearingTypeDirection) {
        return spy(FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .divorceCaseNumber(TEST_DIVORCE_CASE_NUMBER)
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationRejectReason("generalApplicationRejectReason")
                .build())
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailBody("generalEmailBody")
                .build())
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .regionList(Region.HIGHCOURT)
                    .highCourtFrcList(RegionHighCourtFrc.HIGHCOURT)
                    .build())
                .build())
            .listForHearingWrapper(ListForHearingWrapper.builder()
                .hearingType(mockedHearingTypeDirection)
                .build())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .respondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL)
                .respondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE)
                .solicitorReference(TEST_SOLICITOR_REFERENCE)
                .build())
            .intervenerOne(IntervenerOne.builder()
                .intervenerSolName(TEST_INTV1_SOLICITOR_NAME)
                .intervenerSolicitorFirm(TEST_INTV1_SOLICITOR_FIRM)
                .intervenerSolEmail(TEST_INTV1_SOLICITOR_EMAIL)
                .intervenerSolicitorReference(TEST_INTV1_SOLICITOR_REFERENCE)
                .build())
            .build());
    }

    private void verifyEmptyStringInNotificationRequest(NotificationRequest actualResult) {
        assertThat(actualResult)
            .extracting(
                NotificationRequest::getDivorceCaseNumber,
                NotificationRequest::getGeneralApplicationRejectionReason,
                NotificationRequest::getGeneralEmailBody,
                NotificationRequest::getSolicitorReferenceNumber)
            .containsOnly(EMPTY_STRING);
    }

    private void verifyStandardNotificationRequest(CaseType caseType, NotificationRequest actualResult) {
        assertThat(actualResult)
            .extracting(
                NotificationRequest::getCaseReferenceNumber,
                NotificationRequest::getDivorceCaseNumber,
                NotificationRequest::getCaseType,
                NotificationRequest::getPhoneOpeningHours,
                NotificationRequest::getGeneralApplicationRejectionReason,
                NotificationRequest::getGeneralEmailBody,
                NotificationRequest::getApplicantName,
                NotificationRequest::getRespondentName,
                NotificationRequest::getCaseOrderType,
                NotificationRequest::getCamelCaseOrderType,
                NotificationRequest::getSelectedCourt,
                NotificationRequest::getHearingType
            )
            .contains(
                String.valueOf(CASE_ID_IN_LONG),
                TEST_DIVORCE_CASE_NUMBER,
                expectedCaseTypeString(caseType),
                "from 8am to 6pm, Monday to Friday",
                "generalApplicationRejectReason",
                "generalEmailBody",
                "Full Applicant Name",
                format("Full Respondent Name (%s)", (CaseType.CONTESTED.equals(caseType) ? "CT" : "CS")),
                CaseType.CONSENTED.equals(caseType) ? "variation" : null,
                CaseType.CONSENTED.equals(caseType) ? "Variation" : null,
                CaseType.CONTESTED.equals(caseType) ? RegionHighCourtFrc.HIGHCOURT.getValue() : null,
                TEST_HEARING_TYPE
            );
    }

    private String expectedCaseTypeString(CaseType caseType) {
        return switch (caseType) {
            case CONTESTED -> "contested";
            case CONSENTED -> "consented";
            default -> null;
        };
    }

    private static Stream<Arguments> intervenerOrganisationScenarios() {
        Organisation completeOrg = Organisation.builder()
            .organisationName("test org")
            .organisationID("1")
            .build();

        Organisation orgWithNullId = Organisation.builder()
            .organisationName("test org")
            .organisationID(null)
            .build();

        OrganisationPolicy withCompleteOrg = OrganisationPolicy.builder()
            .organisation(completeOrg)
            .build();

        OrganisationPolicy withNullOrg = OrganisationPolicy.builder()
            .organisation(null)
            .build();

        OrganisationPolicy withOrgWithoutId = OrganisationPolicy.builder()
            .organisation(orgWithNullId)
            .build();

        return Stream.of(
            arguments(null, null),                        // intervenerOrganisation is null
            arguments(withNullOrg, null),                // organisation is null
            arguments(withOrgWithoutId, "test org"),     // organisationID is null
            arguments(withCompleteOrg, "test org")       // control case
        );
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

    @Test
    void givenContestedCaseData_whenGeneralEmail_thenBuildNotificationRequest() {
        String emailRecipient = "test@test.com";
        String emailBody = "This is a contested case test email";
        contestedFinremCaseDetails.getData().setGeneralEmailWrapper(GeneralEmailWrapper.builder()
            .generalEmailRecipient(emailRecipient)
            .generalEmailBody(emailBody)
            .build());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForGeneralEmail(contestedFinremCaseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(emailRecipient, notificationRequest.getNotificationEmail());
        assertEquals(emailBody, notificationRequest.getGeneralEmailBody());
        assertEquals("contested", notificationRequest.getCaseType());
        assertEquals("nottingham", notificationRequest.getSelectedCourt());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void givenConsentedCaseData_whenGeneralEmail_thenBuildNotificationRequest() {
        String emailRecipient = "test@test.com";
        String emailBody = "This is a consented case test email";
        consentedFinremCaseDetails.getData().setGeneralEmailWrapper(GeneralEmailWrapper.builder()
            .generalEmailRecipient(emailRecipient)
            .generalEmailBody(emailBody)
            .build());
        NotificationRequest notificationRequest = notificationRequestMapper.getNotificationRequestForGeneralEmail(consentedFinremCaseDetails);

        assertEquals("12345", notificationRequest.getCaseReferenceNumber());
        assertEquals(TEST_SOLICITOR_REFERENCE, notificationRequest.getSolicitorReferenceNumber());
        assertEquals(TEST_DIVORCE_CASE_NUMBER, notificationRequest.getDivorceCaseNumber());
        assertEquals(TEST_SOLICITOR_NAME, notificationRequest.getName());
        assertEquals(emailRecipient, notificationRequest.getNotificationEmail());
        assertEquals("consented", notificationRequest.getCaseType());
        assertEquals("consent", notificationRequest.getCaseOrderType());
        assertEquals("Consent", notificationRequest.getCamelCaseOrderType());
        assertEquals("David Goodman", notificationRequest.getRespondentName());
        assertEquals("Victoria Goodman", notificationRequest.getApplicantName());
    }

    @Test
    void givenCase_whenStopRepresentingEmailToApplicantSolicitor_thenBuildNotificationRequest() {
        NotificationRequestBuilder builder = spy(new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper));
        when(builderFactory.newInstance()).thenReturn(builder);

        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getSolicitorReference()).thenReturn(TEST_SOLICITOR_REFERENCE);
        when(caseData.getAppSolicitorName()).thenReturn(TEST_SOLICITOR_NAME);
        when(caseData.getAppSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        lenient().when(contactDetailsWrapper.getRespondentSolicitorReference())
            .thenReturn(TEST_RESP_SOLICITOR_REFERENCE);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);

        notificationRequestMapper
            .getNotificationRequestForStopRepresentingClientEmail(caseDetails, CaseRole.APP_SOLICITOR);

        verify(builder).withCaseDefaults(caseDetails);
        ArgumentCaptor<SolicitorCaseDataKeysWrapper> captor = ArgumentCaptor.forClass(SolicitorCaseDataKeysWrapper.class);
        verify(builder).withSolicitorCaseData(captor.capture());
        verify(builder).withDateOfIssue();
        verify(builder).withIntervener(isNull());
        verify(builder).build();
        verifyNoMoreInteractions(builder);

        assertThat(captor.getValue())
            .extracting(SolicitorCaseDataKeysWrapper::getSolicitorReferenceKey,
                SolicitorCaseDataKeysWrapper::getSolicitorEmailKey,
                SolicitorCaseDataKeysWrapper::getSolicitorNameKey)
            .contains(TEST_SOLICITOR_REFERENCE, TEST_SOLICITOR_EMAIL, TEST_SOLICITOR_NAME);
    }

    @Test
    void givenCase_whenStopRepresentingEmailToRespondentSolicitor_thenBuildNotificationRequest() {
        NotificationRequestBuilder builder = spy(new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper));
        when(builderFactory.newInstance()).thenReturn(builder);


        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getRespondentSolicitorReference()).thenReturn(TEST_RESP_SOLICITOR_REFERENCE);
        when(caseData.getRespondentSolicitorName()).thenReturn(TEST_RESP_SOLICITOR_NAME);
        when(contactDetailsWrapper.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        lenient().when(contactDetailsWrapper.getSolicitorReference())
            .thenReturn(TEST_SOLICITOR_REFERENCE);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);

        notificationRequestMapper
            .getNotificationRequestForStopRepresentingClientEmail(caseDetails, CaseRole.RESP_SOLICITOR);

        verify(builder).withCaseDefaults(caseDetails);
        ArgumentCaptor<SolicitorCaseDataKeysWrapper> captor = ArgumentCaptor.forClass(SolicitorCaseDataKeysWrapper.class);
        verify(builder).withSolicitorCaseData(captor.capture());
        verify(builder).withDateOfIssue();
        verify(builder).withIntervener(isNull());
        verify(builder).build();
        verifyNoMoreInteractions(builder);

        assertThat(captor.getValue())
            .extracting(SolicitorCaseDataKeysWrapper::getSolicitorReferenceKey,
                SolicitorCaseDataKeysWrapper::getSolicitorEmailKey,
                SolicitorCaseDataKeysWrapper::getSolicitorNameKey)
            .contains(TEST_RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_NAME);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "INTVR_SOLICITOR_1, INTERVENER_ONE",
        "INTVR_SOLICITOR_2, INTERVENER_TWO",
        "INTVR_SOLICITOR_3, INTERVENER_THREE",
        "INTVR_SOLICITOR_4, INTERVENER_FOUR"
    })
    void givenCase_whenStopRepresentingEmailToIntervenerSolicitor_thenBuildNotificationRequest(
        CaseRole caseRole, IntervenerType intervenerType
    ) {

        NotificationRequestBuilder builder = spy(new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper));
        when(builderFactory.newInstance()).thenReturn(builder);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = spy(FinremCaseData.builder().build());

        lenient().when(caseData.getIntervenerById(1)).thenReturn(IntervenerOne.builder()
            .intervenerSolEmail(TEST_INTV_SOLICITOR_EMAIL)
            .intervenerSolName(TEST_INTV_SOLICITOR_NAME)
            .intervenerSolicitorReference(TEST_INTV_SOLICITOR_REFERENCE)
            .build());
        lenient().when(caseData.getIntervenerById(2)).thenReturn(IntervenerTwo.builder()
            .intervenerSolEmail(TEST_INTV_SOLICITOR_EMAIL)
            .intervenerSolName(TEST_INTV_SOLICITOR_NAME)
            .intervenerSolicitorReference(TEST_INTV_SOLICITOR_REFERENCE)
            .build());
        lenient().when(caseData.getIntervenerById(3)).thenReturn(IntervenerThree.builder()
            .intervenerSolEmail(TEST_INTV_SOLICITOR_EMAIL)
            .intervenerSolName(TEST_INTV_SOLICITOR_NAME)
            .intervenerSolicitorReference(TEST_INTV_SOLICITOR_REFERENCE)
            .build());
        lenient().when(caseData.getIntervenerById(4)).thenReturn(IntervenerFour.builder()
            .intervenerSolEmail(TEST_INTV_SOLICITOR_EMAIL)
            .intervenerSolName(TEST_INTV_SOLICITOR_NAME)
            .intervenerSolicitorReference(TEST_INTV_SOLICITOR_REFERENCE)
            .build());

        when(caseDetails.getData()).thenReturn(caseData);

        notificationRequestMapper
            .getNotificationRequestForStopRepresentingClientEmail(caseDetails, caseRole, intervenerType);

        verify(builder).withCaseDefaults(caseDetails);
        ArgumentCaptor<SolicitorCaseDataKeysWrapper> captor = ArgumentCaptor.forClass(SolicitorCaseDataKeysWrapper.class);
        verify(builder).withSolicitorCaseData(captor.capture());
        verify(builder).withDateOfIssue();
        verify(builder).withIntervener(any(IntervenerDetails.class));
        verify(builder).build();
        verifyNoMoreInteractions(builder);

        assertThat(captor.getValue())
            .extracting(SolicitorCaseDataKeysWrapper::getSolicitorReferenceKey,
                SolicitorCaseDataKeysWrapper::getSolicitorEmailKey,
                SolicitorCaseDataKeysWrapper::getSolicitorNameKey)
            .contains(TEST_INTV_SOLICITOR_REFERENCE, TEST_INTV_SOLICITOR_EMAIL, TEST_INTV_SOLICITOR_NAME);
    }

    @Test
    void givenCase_whenStopRepresentingEmailToApplBarrister_thenBuildNotificationRequest() {
        NotificationRequestBuilder builder = spy(new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper));
        when(builderFactory.newInstance()).thenReturn(builder);

        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getSolicitorReference()).thenReturn(TEST_SOLICITOR_REFERENCE);
        caseData.setBarristerCollectionWrapper(BarristerCollectionWrapper.builder()
            .applicantBarristers(barristers(TEST_ORG_ID, TEST_APP_BARRISTER_USER_ID, TEST_APP_BARRISTER_NAME, TEST_APP_BARRISTER_EMAIL))
            .build());

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);

        Barrister barrister = barrister(TEST_ORG_ID, TEST_APP_BARRISTER_USER_ID, TEST_APP_BARRISTER_NAME, TEST_APP_BARRISTER_EMAIL);
        notificationRequestMapper
            .getNotificationRequestForStopRepresentingClientEmail(caseDetails, barrister);

        verify(builder).withCaseDefaults(caseDetails);
        ArgumentCaptor<SolicitorCaseDataKeysWrapper> captor = ArgumentCaptor.forClass(SolicitorCaseDataKeysWrapper.class);
        verify(builder).withSolicitorCaseData(captor.capture());
        verify(builder).withDateOfIssue();
        verify(builder).withIntervener(isNull());
        verify(builder).build();
        verifyNoMoreInteractions(builder);

        assertThat(captor.getValue())
            .extracting(SolicitorCaseDataKeysWrapper::getSolicitorReferenceKey,
                SolicitorCaseDataKeysWrapper::getSolicitorEmailKey,
                SolicitorCaseDataKeysWrapper::getSolicitorNameKey)
            .contains(TEST_SOLICITOR_REFERENCE, TEST_APP_BARRISTER_EMAIL, TEST_APP_BARRISTER_NAME);
    }

    @Test
    void givenCase_whenStopRepresentingEmailToRespBarrister_thenBuildNotificationRequest() {
        NotificationRequestBuilder builder = spy(new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper));
        when(builderFactory.newInstance()).thenReturn(builder);

        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getRespondentSolicitorReference()).thenReturn(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.setBarristerCollectionWrapper(BarristerCollectionWrapper.builder()
            .respondentBarristers(barristers(TEST_ORG_ID, TEST_RESP_BARRISTER_USER_ID, TEST_RESP_BARRISTER_NAME, TEST_RESP_BARRISTER_EMAIL))
            .build());

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);

        Barrister barrister = barrister(TEST_ORG_ID, TEST_RESP_BARRISTER_USER_ID, TEST_RESP_BARRISTER_NAME, TEST_RESP_BARRISTER_EMAIL);
        notificationRequestMapper
            .getNotificationRequestForStopRepresentingClientEmail(caseDetails, barrister);

        verify(builder).withCaseDefaults(caseDetails);
        ArgumentCaptor<SolicitorCaseDataKeysWrapper> captor = ArgumentCaptor.forClass(SolicitorCaseDataKeysWrapper.class);
        verify(builder).withSolicitorCaseData(captor.capture());
        verify(builder).withDateOfIssue();
        verify(builder).withIntervener(isNull());
        verify(builder).build();
        verifyNoMoreInteractions(builder);

        assertThat(captor.getValue())
            .extracting(SolicitorCaseDataKeysWrapper::getSolicitorReferenceKey,
                SolicitorCaseDataKeysWrapper::getSolicitorEmailKey,
                SolicitorCaseDataKeysWrapper::getSolicitorNameKey)
            .contains(TEST_RESP_SOLICITOR_REFERENCE, TEST_RESP_BARRISTER_EMAIL, TEST_RESP_BARRISTER_NAME);
    }

    @Test
    void givenCase_whenStopRepresentingEmailToIntvBarrister_thenBuildNotificationRequest() {
        NotificationRequestBuilder builder = spy(new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper));
        when(builderFactory.newInstance()).thenReturn(builder);

        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(caseData.getIntervenerById(2)).thenReturn(IntervenerTwo.builder()
            .intervenerSolEmail(TEST_INTV_SOLICITOR_EMAIL)
            .intervenerSolName(TEST_INTV_SOLICITOR_NAME)
            .intervenerSolicitorReference(TEST_INTV_SOLICITOR_REFERENCE)
            .build());

        caseData.setBarristerCollectionWrapper(BarristerCollectionWrapper.builder()
            .intvr1Barristers(barristers(TEST_ORG_ID, TEST_INTV_BARRISTER_USER_ID, TEST_INTV_BARRISTER_NAME, TEST_INTV_BARRISTER_EMAIL))
            .build());

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);

        Barrister barrister = barrister(TEST_ORG_ID, TEST_INTV_BARRISTER_USER_ID, TEST_INTV_BARRISTER_NAME, TEST_INTV_BARRISTER_EMAIL);
        notificationRequestMapper
            .getNotificationRequestForStopRepresentingClientEmail(caseDetails, barrister, IntervenerType.INTERVENER_TWO);

        verify(builder).withCaseDefaults(caseDetails);
        ArgumentCaptor<SolicitorCaseDataKeysWrapper> captor = ArgumentCaptor.forClass(SolicitorCaseDataKeysWrapper.class);
        verify(builder).withSolicitorCaseData(captor.capture());
        verify(builder).withDateOfIssue();
        verify(builder).withIntervener(any(IntervenerDetails.class));
        verify(builder).build();
        verifyNoMoreInteractions(builder);

        assertThat(captor.getValue())
            .extracting(SolicitorCaseDataKeysWrapper::getSolicitorReferenceKey,
                SolicitorCaseDataKeysWrapper::getSolicitorEmailKey,
                SolicitorCaseDataKeysWrapper::getSolicitorNameKey)
            .contains(TEST_INTV_SOLICITOR_REFERENCE, TEST_INTV_BARRISTER_EMAIL, TEST_INTV_BARRISTER_NAME);
    }

    private void mockNotificationRequestBuilderFactory() {
        NotificationRequestBuilder builder = new NotificationRequestBuilder(courtDetailsConfiguration, consentedApplicationHelper);
        lenient().when(builderFactory.newInstance()).thenReturn(builder);
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
