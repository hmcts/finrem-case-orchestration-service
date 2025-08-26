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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendConsentOrderMidHandlerTest {

    private AmendConsentOrderMidHandler handler;
    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;
    @Mock
    private ConsentedApplicationHelper helper;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new AmendConsentOrderMidHandler(finremCaseDetailsMapper, bulkPrintDocumentService, helper);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.AMEND_CONSENT_ORDER);
    }

    @Test
    void givenContestedCase_whenAmendedConsentOrderUploadedNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = mock(CaseDocument.class);

        List<AmendedConsentOrderCollection> amendedCollection = new  ArrayList<>();

        AmendedConsentOrderCollection order = AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder().amendedConsentOrder(caseDocument).build()).build();

        amendedCollection.add(order);
        caseData.setAmendedConsentOrderCollection(amendedCollection);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
        verify(helper).setConsentVariationOrderLabelField(caseData);
    }

    @Test
    void givenContestedCase_whenAmendedConsentOrderUploadedNonEncryptedFileButThereIsAlreadySameDocument_thenDoNotCheck() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = mock(CaseDocument.class);

        List<AmendedConsentOrderCollection> amendedCollection = new  ArrayList<>();

        AmendedConsentOrderCollection order = AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder().amendedConsentOrder(caseDocument).build()).build();

        amendedCollection.add(order);
        caseData.setAmendedConsentOrderCollection(amendedCollection);
        finremCallbackRequest.getCaseDetailsBefore().getData().setAmendedConsentOrderCollection(amendedCollection);;

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(bulkPrintDocumentService, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
        verify(helper).setConsentVariationOrderLabelField(caseData);
    }

    private FinremCallbackRequest buildBaseCallbackRequest() {
        FinremCallbackRequest mockedCallbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails mockedCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseDetails mockedCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(mockedCallbackRequest.getCaseDetails()).thenReturn(mockedCaseDetails);
        when(mockedCallbackRequest.getCaseDetailsBefore()).thenReturn(mockedCaseDetailsBefore);

        FinremCaseData mockedCaseData = spy(FinremCaseData.class);
        when(mockedCaseDetails.getData()).thenReturn(mockedCaseData);
        when(mockedCaseDetails.getId()).thenReturn(Long.valueOf(CASE_ID));

        FinremCaseData mockedCaseDataBefore = spy(FinremCaseData.class);
        when(mockedCaseDetailsBefore.getData()).thenReturn(mockedCaseDataBefore);
        return mockedCallbackRequest;
    }
}
