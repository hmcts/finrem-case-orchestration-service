package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class HearingConsentMidHandlerTest extends BaseHandlerTestSetup {

    private HearingConsentMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    public static final String AUTH_TOKEN = "tokien:)";


    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new HearingConsentMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.LIST_FOR_HEARING_CONSENTED));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED));
    }

    @Test
    void givenContestedCase_whenListForHearingbutNoAdditionalUploaded_thenNoCheckPerformed() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.LIST_FOR_HEARING);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        List<ConsentedHearingDataWrapper> listForHearings =
            Optional.ofNullable(caseData.getListForHearings()).orElse(new ArrayList<>());

        ConsentedHearingDataElement hearingDataElement = ConsentedHearingDataElement.builder()
            .hearingType("ssss")
            .promptForAnyDocument("No")
            .build();
        ConsentedHearingDataWrapper hearingDataWrapper = ConsentedHearingDataWrapper.builder().value(hearingDataElement).build();
        listForHearings.add(hearingDataWrapper);
        caseData.setListForHearings(listForHearings);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenListForHearingAdditionalUploadedButNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.LIST_FOR_HEARING);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        List<ConsentedHearingDataWrapper> listForHearings =
            Optional.ofNullable(caseData.getListForHearings()).orElse(new ArrayList<>());

        ConsentedHearingDataElement hearingDataElement = ConsentedHearingDataElement.builder()
            .hearingType("ssss")
            .promptForAnyDocument("Yes")
            .uploadAdditionalDocument(caseDocument)
            .build();
        ConsentedHearingDataWrapper hearingDataWrapper = ConsentedHearingDataWrapper.builder().value(hearingDataElement).build();
        listForHearings.add(hearingDataWrapper);
        caseData.setListForHearings(listForHearings);


        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenListForHearingAdditionalUploadedButSameDocumentAlreadyExists_thenDoNotCheckDocumentValidity() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.LIST_FOR_HEARING);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        List<ConsentedHearingDataWrapper> listForHearings =
            Optional.ofNullable(caseData.getListForHearings()).orElse(new ArrayList<>());

        ConsentedHearingDataElement hearingDataElement = ConsentedHearingDataElement.builder()
            .hearingType("ssss")
            .promptForAnyDocument("Yes")
            .uploadAdditionalDocument(caseDocument)
            .build();
        ConsentedHearingDataWrapper hearingDataWrapper = ConsentedHearingDataWrapper.builder().value(hearingDataElement).build();
        listForHearings.add(hearingDataWrapper);
        caseData.setListForHearings(listForHearings);

        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        caseDataBefore.setListForHearings(listForHearings);


        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }
}