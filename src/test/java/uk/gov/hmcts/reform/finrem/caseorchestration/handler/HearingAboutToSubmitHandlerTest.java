package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@RunWith(MockitoJUnitRunner.class)
public class HearingAboutToSubmitHandlerTest {

    @InjectMocks
    private HearingAboutToSubmitHandler handler;
    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Mock
    private ValidateHearingService validateHearingService;
    @Mock
    private CaseDataService caseDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String FAST_TRACK_JSON = "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    private static final String FAST_TRACK_PAPER_JSON = "/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json";
    private static final String ADDITIONAL_DOC_JSON = "/fixtures/bulkprint/bulk-print-additional-hearing.json";
    private static final String NON_FAST_TRACK_JSON = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";
    private static final String VALID_HEARING_JSON = "/fixtures/contested/validate-hearing-successfully.json";

    @Test
    public void givenCase_whenValid_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    public void givenCase_whenInValidCaseType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenCase_whenInValidEventType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenCase_whenInValidCallbackType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenContestedCase_whenHearingWithFastTrackDecision_thenGenerateHearingDocumentFormC() {
        CallbackRequest callbackRequest = buildCallbackRequest(FAST_TRACK_JSON);
        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        CaseDocument formC = convertToCaseDocument(caseData.get("formC"));

        assertEquals(DOC_URL, formC.getDocumentUrl());
        assertEquals(FILE_NAME, formC.getDocumentFilename());
        assertEquals(BINARY_URL, formC.getDocumentBinaryUrl());

        verify(hearingDocumentService, never()).sendFormCAndGForBulkPrint(any(), any());
        verify(hearingDocumentService).alreadyHadFirstHearing(any());
    }

    @Test
    public void givenContestedCase_whenPaperApplication_thenGenerateHearingDocument() {
        CallbackRequest callbackRequest = buildCallbackRequest(FAST_TRACK_PAPER_JSON);
        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        CaseDocument formC = convertToCaseDocument(caseData.get("formC"));

        assertEquals(DOC_URL, formC.getDocumentUrl());
        assertEquals(FILE_NAME, formC.getDocumentFilename());
        assertEquals(BINARY_URL, formC.getDocumentBinaryUrl());

        verify(hearingDocumentService, never()).sendFormCAndGForBulkPrint(any(), any());
    }

    @Test(expected = FeignException.InternalServerError.class)
    public void givenContestedCaseGenerateMiniFormA_whenHttpError500_thenHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest(FAST_TRACK_JSON);

        when(hearingDocumentService.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenThrow(feignError());

        handler.handle(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void givenContestedCase_whenGenerateAdditionalHearingDocument_thenHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest(ADDITIONAL_DOC_JSON);
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);
        doNothing().when(additionalHearingDocumentService).createAdditionalHearingDocuments(any(), any());
        final String url
            = "http://dm-store-aat.service.core-compute-aat.internal/documents/4f73432d-2889-47eb-bc16-84d021222dbc";
        final String filename = "Form-C.pdf";
        final String binaryUrl
            = "http://dm-store-aat.service.core-compute-aat.internal/documents/4f73432d-2889-47eb-bc16-84d021222dbc/binary";

        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        CaseDocument formC = convertToCaseDocument(caseData.get("formC"));

        assertEquals(url, formC.getDocumentUrl());
        assertEquals(filename, formC.getDocumentFilename());
        assertEquals(binaryUrl, formC.getDocumentBinaryUrl());

        verify(hearingDocumentService, never()).sendFormCAndGForBulkPrint(any(), any());
        verify(caseDataService).isContestedPaperApplication(any());
        verify(additionalHearingDocumentService).createAdditionalHearingDocuments(any(),any());
    }

    @Test
    public void givenContestedCase_whenNotFastTrackDecision_thenShouldThrowWarnings() {
        CallbackRequest callbackRequest = buildCallbackRequest(NON_FAST_TRACK_JSON);
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
            .thenReturn(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals("Date of the hearing must be between 12 and 14 weeks.", response.getWarnings().get(0));
    }

    @Test
    public void givenContestedCase_whenFastTrackDecision_thenShouldThrowWarnings() {
        CallbackRequest callbackRequest = buildCallbackRequest(VALID_HEARING_JSON);
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class))).thenReturn(ImmutableList.of());

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(response.getWarnings().isEmpty());
    }


    @Test
    public void givenContestedCase_whenValidCaseData_thenShouldSuccessfullyValidate() {
        CallbackRequest callbackRequest = buildCallbackRequest(FAST_TRACK_JSON);
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
            .thenReturn(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."));

        AboutToStartOrSubmitCallbackResponse response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals("Date of the hearing must be between 12 and 14 weeks.", response.getWarnings().get(0));
    }

    private CallbackRequest buildCallbackRequest(final String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}
