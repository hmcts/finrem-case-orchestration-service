package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.io.File;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(HearingController.class)
public class HearingControllerTest extends BaseControllerTest {

    private static final String VALIDATE_HEARING_PATH = "/case-orchestration/validate-hearing";
    private static final String SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH = "/case-orchestration/submit-scheduling-and-listing-details";

    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY =
        "Issue Date, fast track decision or hearingDate is empty";

    @Autowired private ObjectMapper objectMapper;

    @MockBean private ValidateHearingService validateHearingService;
    @MockBean private HearingDocumentService hearingDocumentService;
    @MockBean private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean private NotificationService notificationService;

    @Before
    public void setUp()  {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(emptyList());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class))).thenReturn(emptyList());
    }

    private void doRequestSetUp() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json").toURI()));
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmptyCaseDataSetUp();
        mvc.perform(post("/case-orchestration/validate-hearing")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(startsWith(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void shouldThrowErrorWhenIssueDateAndHearingDateAreEmpty() throws Exception {
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class)))
                .thenReturn(ImmutableList.of(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));
        mvc.perform(post(VALIDATE_HEARING_PATH)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]",
                        Matchers.is(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY)));
    }

    @Test
    public void shouldThrowWarningsWhenNotFastTrackDecision() throws Exception {
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
                .thenReturn(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."));

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-withoutfastTrackDecision.json").toURI()));
        mvc.perform(post(VALIDATE_HEARING_PATH)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.warnings[0]",
                        Matchers.is("Date of the hearing must be between 12 and 14 weeks.")));
    }

    @Test
    public void shouldThrowWarningsWhenFastTrackDecision() throws Exception {
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
                .thenReturn(ImmutableList.of("Date of the Fast Track hearing must be between 6 and 10 weeks."));

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json").toURI()));
        mvc.perform(post(VALIDATE_HEARING_PATH)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.warnings[0]",
                        Matchers.is("Date of the Fast Track hearing must be between 6 and 10 weeks.")));
    }

    @Test
    public void shouldSuccessfullyValidate() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-successfully.json").toURI()));
        mvc.perform(post(VALIDATE_HEARING_PATH)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.warnings").isEmpty());
    }

    @Test
    public void generateHearingDocumentHttpError400() throws Exception {
        mvc.perform(post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH)
            .content("kwuilebge")
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void generateHearingDocumentFormC() throws Exception {
        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));

        verify(hearingDocumentService, never()).sendFormCAndGForBulkPrint(any(), any());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void generateHearingDocumentPaperApplication() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json").toURI()));

        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));

        verify(hearingDocumentService, times(1)).sendFormCAndGForBulkPrint(isA(CaseDetails.class), eq(AUTH_TOKEN));
        verify(notificationService, times(1)).sendPrepareForHearingEmail(any());
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenThrow(feignError());

        mvc.perform(post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void generateAdditionalHearingDocumentSuccess() throws Exception {
        doValidCaseDataSetUpForAdditionalHearing();

        mvc.perform(post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(hearingDocumentService,  times(0)).generateHearingDocuments(any(), any());
        verify(additionalHearingDocumentService, times(1)).createAndSendAdditionalHearingDocuments(any(), any());
    }

    @Test
    public void generateAdditionalHearingDocumentWhenOneAlreadyExists() throws Exception {
        mvc.perform(post(SUBMIT_SCHEDULING_AND_LISTING_DETAILS_PATH)
            .content(resourceContentAsString("/fixtures/bulkprint/bulk-print-additional-hearing-exists.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(hearingDocumentService,  times(0)).generateHearingDocuments(any(), any());
        verify(additionalHearingDocumentService, times(1)).createAndSendAdditionalHearingDocuments(any(), any());
    }
}
