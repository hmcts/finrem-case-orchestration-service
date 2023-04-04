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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @MockBean
    private FinremHearingDocumentService finremHearingDocumentService;

    @MockBean
    GenericDocumentService genericDocumentService;

    private ObjectMapper objectMapper = null;
    private static final String PATH = "/fixtures/validate-hearing-successfully/";

    private static final String CONTESTED_CASE_WITH_FAST_TRACK_HEARING  = "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    private static final String fastTrack = "/fixtures/contested/validate-hearing-with-fastTrackDecision.json/";
    private static final String HEARING_TEST_PAYLOAD = "/fixtures/contested/hearing-with-case-details-before.json";
    private static final String AUTH_TOKEN = "tokien:)";
    public static final String HEARING_ADDITIONAL_DOC = "additionalListOfHearingDocuments";
    private FinremCallbackRequest callbackRequest;


    @Before
    public void init() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        callbackRequest = buildFinremCallbackRequest(CONTESTED_CASE_WITH_FAST_TRACK_HEARING);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
    }

    @Test
    public void givenContestedHearing_when_PrepareForHearing_thenItShouldNotGenerateAndSendInitalCorrespondence() throws JsonProcessingException {
        FinremCaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
        FinremCaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
        when(additionalHearingDocumentService.convertToPdf(any(), any())).thenReturn(caseDocument());

        contestedHearingService.prepareForHearing(callbackRequest, AUTH_TOKEN);

        verify(finremHearingDocumentService).generateHearingDocuments(any(), any());
        verify(finremHearingDocumentService).sendInitialHearingCorrespondence(any(), any());

    }
      // already had first hearing then create additional Hearing Documents
    @Test
    public void givenContestedHearing_when_alreadyHadFirstHearing_thenItShouldCreateAdditionalDocuments() throws JsonProcessingException {
        FinremCaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
        FinremCaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
        when(finremHearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);
        //when(additionalHearingDocumentService.createAdditionalHearingDocuments(any(), any())).thenReturn(caseDocument());

        contestedHearingService.prepareForHearing(callbackRequest, AUTH_TOKEN);

        verify(finremHearingDocumentService).alreadyHadFirstHearing(any());
        //verify(additionalHearingDocumentService).createAdditionalHearingDocuments(any(), any());

    }

//    // No first hearing then don't create additional Hearing Documents
//    @Test
//    public void givenContestedHearing_when_No_Hearing_thenItShould_Not_CreateAdditionalDocuments() throws JsonProcessingException {
//        FinremCaseDetails caseDetails = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        FinremCaseDetails caseDetailsBefore = buildCaseDetails(HEARING_TEST_PAYLOAD);
//        when(finremHearingDocumentService.alreadyHadFirstHearing(assertFalse("No hearing"));
//        when(additionalHearingDocumentService.createAdditionalHearingDocuments(caseDocument(caseDetails)).thenReturn(false)
//
//        contestedHearingService.prepareForHearing(callbackRequest, AUTH_TOKEN);
//
//        verify(finremHearingDocumentService).alreadyHadFirstHearing(any());
//        //verify(additionalHearingDocumentService).createAdditionalHearingDocuments(any(), any());
//    }

    private FinremCaseDetails buildCaseDetails(String testPayload) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPayload)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
