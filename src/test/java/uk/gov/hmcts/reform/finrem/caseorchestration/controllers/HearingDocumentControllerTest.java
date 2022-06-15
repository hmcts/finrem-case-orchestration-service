package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

@WebMvcTest(HearingDocumentController.class)
public class HearingDocumentControllerTest extends BaseControllerTest {

    private static final String DIRECTION_ORDER_URL = "/case-orchestration/contested-upload-direction-order";
    private static final String VALIDATE_AND_GEN_DOC_URL = "/case-orchestration/documents/hearing";
    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY = "Issue Date, fast track decision or hearingDate is empty";

    @MockBean
    private HearingDocumentService hearingDocumentService;
    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private ValidateHearingService validateHearingService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private NotificationService notificationService;


    @Before
    public void setUp() {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")).toURI()));
    }

    @Test
    public void generateHearingDocumentHttpError400() throws Exception {
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void generateHearingDocumentFormC() throws Exception {
        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));

        verify(hearingDocumentService, never()).sendFormCAndGForBulkPrint(any(), any());
    }

    @Test
    public void generateHearingDocumentPaperApplication() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json")).toURI()));

        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenThrow(feignError());

        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void generateAdditionalHearingDocumentSuccess() throws Exception {
        doValidCaseDataSetUpForAdditionalHearing();

        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);

        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(hearingDocumentService, times(0)).generateHearingDocuments(eq(AUTH_TOKEN), any());
        verify(additionalHearingDocumentService, times(1)).createAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
    }

    @Test
    public void generateHearingDocumentDirectionOrder_isAnotherHearingTrue() throws Exception {
        mvc.perform(post(DIRECTION_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(additionalHearingDocumentService, times(1)).createAndStoreAdditionalHearingDocuments(any(), any());
    }

    @Test
    public void generateHearingDocumentDirectionOrder_CourtDetailsParseException() throws Exception {
        doThrow(new CourtDetailsParseException()).when(additionalHearingDocumentService).createAndStoreAdditionalHearingDocuments(any(), any());

        mvc.perform(post(DIRECTION_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]", is(new CourtDetailsParseException().getMessage())));
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmptyCaseDataSetUp();
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
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

        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/pba-validate.json")).toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
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
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
            .thenReturn(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."));

        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-withoutfastTrackDecision.json")).toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
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
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
            .thenReturn(ImmutableList.of("Date of the Fast Track hearing must be between 6 and 10 weeks."));

        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")).toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
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
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class))).thenReturn(ImmutableList.of());

        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-successfully.json")).toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.warnings").isEmpty());
    }

    @Test
    public void givenNoPreviousHearing_shouldPrintHearingDocumentsForRespondentSolicitor() throws Exception {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(false);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/hearing-with-case-details-before.json").toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(hearingDocumentService).sendFormCAndGForBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenNoPreviousHearing_shouldPrintHearingDocumentsForApplicantSolicitor() throws Exception {
        when(notificationService.shouldEmailContestedAppSolicitor(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(false);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/hearing-with-case-details-before.json").toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(hearingDocumentService).sendFormCAndGForBulkPrint(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenHadPreviousHearing_thenPrintAdditionalHearingDocumentsForRespondentSolicitor() throws Exception {
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/hearing-with-case-details-before.json").toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
    }

    @Test
    public void givenHadPreviousHearing_thenPrintAdditionalHearingDocumentsForApplicantSolicitor() throws Exception {
        when(notificationService.shouldEmailContestedAppSolicitor(any())).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/hearing-with-case-details-before.json").toURI()));
        mvc.perform(post(VALIDATE_AND_GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
    }

    public void generateHearingDocumentDirectionOrderMostRecentEnteredAtTheTop() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-successfully.json")).toURI()));
        mvc.perform(post(DIRECTION_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.directionDetailsCollection[0].value.dateOfHearing", is("2020-07-01")))
            .andExpect(jsonPath("$.data.directionDetailsCollection[1].value.dateOfHearing", is("2022-12-01")))
            .andExpect(jsonPath("$.data.directionDetailsCollection[2].value.dateOfHearing", is("2023-10-01")));

        verify(additionalHearingDocumentService, times(1)).createAndStoreAdditionalHearingDocuments(any(), any());

    }
}

