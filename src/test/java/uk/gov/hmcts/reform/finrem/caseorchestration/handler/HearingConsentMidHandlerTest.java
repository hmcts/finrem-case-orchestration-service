package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class HearingConsentMidHandlerTest {

    private HearingConsentMidHandler handler;

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new HearingConsentMidHandler(finremCaseDetailsMapper, bulkPrintDocumentService);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED);
    }

    @Test
    void givenContestedCase_whenListForHearingButNoAdditionalUploaded_thenNoCheckPerformed() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(Long.valueOf(CASE_ID), CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED);
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

        assertThat(response.getErrors()).isEmpty();
        verify(bulkPrintDocumentService, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenListForHearingAdditionalUploadedButNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(Long.valueOf(CASE_ID), CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = caseDocument();
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
        verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenListForHearingAdditionalUploadedButSameDocumentAlreadyExists_thenDoNotCheckDocumentValidity() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(Long.valueOf(CASE_ID), CaseType.CONSENTED, EventType.LIST_FOR_HEARING_CONSENTED);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = caseDocument();
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
        verify(bulkPrintDocumentService, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }
}
