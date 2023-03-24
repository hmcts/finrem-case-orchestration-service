package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;

public class ContestedHearingServiceTest extends BaseServiceTest {



    @Autowired
    private ContestedHearingService contestedHearingService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private HearingDocumentService hearingDocumentService;
    @MockBean
    private ValidateHearingService validateHearingService;
    @MockBean
    private CaseDataService caseDataService;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PATH = "/fixtures/validate-hearing-successfully/";

    private static final String CONTESTED_CASE_WITH_FAST_TRACK_HEARING  = "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    private static final String fastTrack = "/fixtures/contested/validate-hearing-with-fastTrackDecision.json/";
    private static final String HEARING_TEST_PAYLOAD = "/fixtures/contested/hearing-with-case-details-before.json";
    private static final String AUTH_TOKEN = "tokien:)";
    public static final String HEARING_ADDITIONAL_DOC = "additionalListOfHearingDocuments";
    private FinremCallbackRequest callbackRequest;


    @Before
    public void init() throws Exception {
        callbackRequest = buildFinremCallbackRequest(CONTESTED_CASE_WITH_FAST_TRACK_HEARING);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
    }


    @Test
    public void givenContestedHearing_whenNot_PrepareForHearing_thenItShouldNotGenerateAndSendInitalCorrespondence() throws JsonProcessingException {
        FinremCaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
        FinremCaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
        when(additionalHearingDocumentService.convertToPdf(any(), any())).thenReturn(caseDocument());

        contestedHearingService.prepareForHearing(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService).generateHearingDocuments(any(), any());
        verify(hearingDocumentService).sendInitialHearingCorrespondence(any(), any());

    }

//    // Create additional hearing documents test
//    @Test
//    public void givenContestedHearing_whenFirstHearing_thenItShould() {
//        CaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        CaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        when(hearingDocumentService.alreadyHadFirstHearing(any(), any(), any(), any().thenReturn(caseDocument())));
//        when(hearingDocumentService.generateHearingDocuments(any(), any()).thenReturn(caseDocument()));
//
//        when(caseDataService.isContestedApplication(caseDetails).thenReturn(true));
//        when(caseDataService.isContestedApplication(caseDetailsBefore).thenReturn(true));
//
//        contestedHearingService.(caseDetails, caseDetailsBefore);
//    }
//
//    @Test
//    public void givenContestedPaperCase_WhenPaperCase_ThenItShouldNotSendNotificaton() {
//        CaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        CaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
//
//        caseDetails.getData().put("paperApplication", "Yes");
//
//        when(caseDataService.isPaperApplication(any())).thenReturn(true);
//
//        contestedHearingService.pr(caseDetails, caseDetailsBefore);
//
//        verify(caseDataService).isPaperApplication(any());
//        verify(caseDataService, never()).isApplicantSolicitorAgreeToReceiveEmails(any());
//        verify(notificationService, never()).isRespondentSolicitorEmailCommunicationEnabled(any());
//        verify(notificationService, never()).sendContestedGeneralApplicationOutcomeEmail(any(), anyMap());
//        verify(notificationService, never()).sendContestedHearingNotificationEmailToRespondentSolicitor(any(), anyMap());
//    }
//
//    @Test
//    public void givenContestedNotPaperCase_WhenPaperCase_ThenItShouldSendNotificaton() {
//        CaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        caseDetails.getData().put("paperApplication", NO_VALUE);
//
//        when(caseDataService.isPaperApplication(any())).thenReturn(false);
//        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
//        when(notificationService.isRespondentSolicitorEmailCommunicationEnabled(any())).thenReturn(true);
//
//        CaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(caseDetails, caseDetailsBefore);
//
//        verify(caseDataService).isPaperApplication(any());
//
//        verify(caseDataService, times(2)).isApplicantSolicitorAgreeToReceiveEmails(any());
//        verify(notificationService, times(2)).isRespondentSolicitorEmailCommunicationEnabled(any());
//        verify(notificationService, times(2)).sendContestedApplicationIssuedEmailToApplicantSolicitor(any(), any());
//        verify(notificationService, times(2)).sendContestedHearingNotificationEmailToRespondentSolicitor(any(), anyMap());
//    }


    private FinremCaseDetails buildCaseDetails(String testPayload) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPayload)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
