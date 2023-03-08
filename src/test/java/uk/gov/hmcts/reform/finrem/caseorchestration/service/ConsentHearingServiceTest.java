package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class ConsentHearingServiceTest extends BaseServiceTest  {

    @Autowired
    private ConsentHearingService service;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CaseDataService caseDataService;
    @Autowired
    private ConsentedHearingHelper helper;

    private ObjectMapper objectMapper;
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String SINGLE_HEARING_TEST_PAYLOAD = "/fixtures/consented.listOfHearing/list-for-hearing.json";
    private static final String MULTIPLE_HEARING_TEST_PAYLOAD = "/fixtures/consented.listOfHearing/list-for-hearing-multiple.json";

    @Before
    public void setup() {
        objectMapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    }

    @Test
    public void givenConsentedHearing_whenNoConsentToEmail_thenItShouldSendAllToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);
        CaseDetails caseDetailsBefore = buildCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetailsBefore)).thenReturn(true);

        service.submitHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseDetails.getData());
        assertEquals("2012-05-19", hearings.get(0).getValue().getHearingDate());
    }

    @Test
    public void givenConsentedMultipleHearing_whenNoConsentToEmail_thenItShouldSendAllToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        CaseDetails caseDetailsBefore = buildCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetailsBefore)).thenReturn(true);

        service.submitHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseDetails.getData());
        assertEquals("2012-05-19", hearings.get(0).getValue().getHearingDate());
        assertEquals("2023-12-10", hearings.get(1).getValue().getHearingDate());
    }

    @Test
    public void givenConsentedMultipleHearing_whenOneExistingHearingAndAddedAnotherOne_thenItShouldSendAllToBulkPrint() {
        CaseDetails caseDetails = buildCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        CaseDetails caseDetailsBefore = buildCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetailsBefore)).thenReturn(true);
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        service.submitHearing(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<ConsentedHearingDataWrapper> hearings = helper.getHearings(caseDetails.getData());
        assertEquals("2012-05-19", hearings.get(0).getValue().getHearingDate());
        assertEquals("2023-12-10", hearings.get(1).getValue().getHearingDate());
    }


    @Test
    public void givenConsentedPaperCase_WhenPaperCase_ThenItShouldNotSendNotification() {
        CaseDetails caseDetails = buildCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        CaseDetails caseDetailsBefore = buildCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);

        caseDetails.getData().put("paperApplication", "Yes");

        when(caseDataService.isPaperApplication(any())).thenReturn(true);

        service.sendNotification(caseDetails, caseDetailsBefore);

        verify(caseDataService).isPaperApplication(any());
        verify(caseDataService, never()).isApplicantSolicitorAgreeToReceiveEmails(any());
        verify(notificationService, never()).isRespondentSolicitorEmailCommunicationEnabled(any());
        verify(notificationService, never()).sendConsentHearingNotificationEmailToApplicantSolicitor(any(CaseDetails.class), anyMap());
        verify(notificationService, never()).sendConsentHearingNotificationEmailToRespondentSolicitor(any(CaseDetails.class), anyMap());
    }

    @Test
    public void givenFinremCaseDetailsConsentedPaperCase_WhenPaperCase_ThenItShouldNotSendNotification() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        FinremCaseDetails caseDetailsBefore = buildFinremCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);

        caseDetails.getData().setPaperApplication(YesOrNo.YES);

        service.sendNotification(caseDetails, caseDetailsBefore);

        verify(caseDataService, never()).isApplicantSolicitorAgreeToReceiveEmails(any());
        verify(notificationService, never()).isRespondentSolicitorEmailCommunicationEnabled(any());
        verify(notificationService, never())
            .sendConsentHearingNotificationEmailToApplicantSolicitor(any(FinremCaseDetails.class), anyMap());
        verify(notificationService, never())
            .sendConsentHearingNotificationEmailToRespondentSolicitor(any(FinremCaseDetails.class), anyMap());
    }

    @Test
    public void givenConsentedNotPaperCase_WhenPaperCase_ThenItShouldSendNotification() {
        CaseDetails caseDetails = buildCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        caseDetails.getData().put("paperApplication", NO_VALUE);

        when(caseDataService.isPaperApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);

        CaseDetails caseDetailsBefore = buildCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);
        service.sendNotification(caseDetails, caseDetailsBefore);

        verify(caseDataService).isPaperApplication(any());

        verify(caseDataService, times(2)).isApplicantSolicitorAgreeToReceiveEmails(any());
        verify(notificationService, times(2)).isRespondentSolicitorEmailCommunicationEnabled(any());
        verify(notificationService, times(2)).sendConsentHearingNotificationEmailToApplicantSolicitor(any(CaseDetails.class), anyMap());
        verify(notificationService, times(2)).sendConsentHearingNotificationEmailToRespondentSolicitor(any(CaseDetails.class), anyMap());
    }

    @Test
    public void givenFinremCaseDetailsConsentedNotPaperCase_WhenPaperCase_ThenItShouldSendNotification() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails(MULTIPLE_HEARING_TEST_PAYLOAD);
        caseDetails.getData().setPaperApplication(YesOrNo.NO);
        caseDetails.getData().getContactDetailsWrapper().setSolicitorAgreeToReceiveEmails(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        caseDetails.getData().getContactDetailsWrapper().setSolicitorEmail("some@som.com");
        caseDetails.getData().setRespSolNotificationsEmailConsent(YesOrNo.YES);
        FinremCaseDetails caseDetailsBefore = buildFinremCaseDetails(SINGLE_HEARING_TEST_PAYLOAD);

        service.sendNotification(caseDetails, caseDetailsBefore);

        verify(notificationService, times(2))
            .sendConsentHearingNotificationEmailToApplicantSolicitor(any(FinremCaseDetails.class), anyMap());
        verify(notificationService, times(2))
            .sendConsentHearingNotificationEmailToRespondentSolicitor(any(FinremCaseDetails.class), anyMap());
    }

    private CaseDetails buildCaseDetails(String testPayload)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPayload)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FinremCaseDetails buildFinremCaseDetails(String testPayload)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPayload)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}