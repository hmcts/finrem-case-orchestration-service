package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsentOrderInContestedMidHandlerTest  extends BaseHandlerTestSetup {

    private ConsentOrderInContestedMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    public static final String AUTH_TOKEN = "tokien:)";


    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new ConsentOrderInContestedMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CONSENT_ORDER));
    }


    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CONSENT_ORDER));
    }


    @Test
    void givenContestedCase_whenConentorderCreated_thenCheckIfAnyFileContainsEncryption() throws Exception {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.CONSENT_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        caseData.setConsentOrder(caseDocument);

        ConsentOrderWrapper consentOrderWrapper = new ConsentOrderWrapper();
        consentOrderWrapper.setConsentD81Joint(caseDocument);
        consentOrderWrapper.setConsentD81Applicant(caseDocument);
        consentOrderWrapper.setConsentD81Respondent(caseDocument);
        List<OtherDocumentCollection> otherCollection = new ArrayList<>();
        OtherDocumentCollection documentCollection = OtherDocumentCollection.builder()
            .value(OtherDocument.builder().uploadedDocument(caseDocument).build()).build();

        otherCollection.add(documentCollection);
        consentOrderWrapper.setConsentOtherCollection(otherCollection);

        caseData.setConsentOrderWrapper(consentOrderWrapper);

        caseData.setConsentVariationOrderDocument(caseDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, times(6))
            .validateEncryptionOnUploadedDocument(any(CaseDocument.class), anyString(), anyList(), anyString());
    }

    @Test
    void givenContestedCase_whenConentorderCreated_thenCheckIfAnyFileContainsEncryptionOnlyNewlyUpdatedDoc() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.CONSENT_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        caseData.setConsentOrder(caseDocument);

        ConsentOrderWrapper consentOrderWrapper = new ConsentOrderWrapper();
        consentOrderWrapper.setConsentD81Joint(caseDocument);
        consentOrderWrapper.setConsentD81Applicant(caseDocument);
        consentOrderWrapper.setConsentD81Respondent(caseDocument);
        List<OtherDocumentCollection> otherCollection = new ArrayList<>();
        OtherDocumentCollection documentCollection = OtherDocumentCollection.builder()
            .value(OtherDocument.builder().uploadedDocument(caseDocument).build()).build();

        otherCollection.add(documentCollection);
        consentOrderWrapper.setConsentOtherCollection(otherCollection);

        caseData.setConsentOrderWrapper(consentOrderWrapper);

        caseData.setConsentVariationOrderDocument(caseDocument);
        FinremCaseData before = finremCallbackRequest.getCaseDetailsBefore().getData();
        before.setConsentOrderWrapper(consentOrderWrapper);
        before.setConsentVariationOrderDocument(caseDocument);

        List<PensionTypeCollection> consentPensionCollection = new ArrayList<>();

        PensionTypeCollection typeCollection = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder().typeOfDocument(PensionDocumentType.FORM_P1)
                .pensionDocument(caseDocument).build()).build();
        consentPensionCollection.add(typeCollection);
        before.setConsentPensionCollection(consentPensionCollection);
        caseData.setConsentPensionCollection(consentPensionCollection);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, times(5))
            .validateEncryptionOnUploadedDocument(any(CaseDocument.class), anyString(), anyList(), anyString());
    }

    @Test
    void givenContestedCase_whenConentorderCreated_thenCheckIfAnyFileContainsEncryptionOnlyNewlyUpdatedDoc2() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.CONSENT_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        caseData.setConsentOrder(caseDocument);

        ConsentOrderWrapper consentOrderWrapper = new ConsentOrderWrapper();
        consentOrderWrapper.setConsentD81Joint(caseDocument);
        consentOrderWrapper.setConsentD81Applicant(caseDocument);
        consentOrderWrapper.setConsentD81Respondent(caseDocument);
        List<OtherDocumentCollection> otherCollection = new ArrayList<>();
        OtherDocumentCollection documentCollection = OtherDocumentCollection.builder()
            .value(OtherDocument.builder().uploadedDocument(caseDocument).build()).build();

        otherCollection.add(documentCollection);
        consentOrderWrapper.setConsentOtherCollection(otherCollection);

        caseData.setConsentOrderWrapper(consentOrderWrapper);

        caseData.setConsentVariationOrderDocument(caseDocument);
        FinremCaseData before = finremCallbackRequest.getCaseDetailsBefore().getData();
        before.setConsentOrderWrapper(consentOrderWrapper);
        before.setConsentVariationOrderDocument(caseDocument);

        List<PensionTypeCollection> consentPensionCollection = new ArrayList<>();

        PensionTypeCollection typeCollection = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder().typeOfDocument(PensionDocumentType.FORM_P1)
                .pensionDocument(caseDocument).build()).build();
        consentPensionCollection.add(typeCollection);
        before.setConsentPensionCollection(consentPensionCollection);

        List<PensionTypeCollection> consentPensionCollection2 = new ArrayList<>();

        PensionTypeCollection typeCollection2 = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder().typeOfDocument(PensionDocumentType.FORM_P1)
                .pensionDocument(caseDocument).build()).build();
        consentPensionCollection2.add(typeCollection2);
        PensionTypeCollection typeCollection3 = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder().typeOfDocument(PensionDocumentType.FORM_PPF1)
                .pensionDocument(caseDocument).build()).build();
        consentPensionCollection2.add(typeCollection3);
        caseData.setConsentPensionCollection(consentPensionCollection2);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, times(6))
            .validateEncryptionOnUploadedDocument(any(CaseDocument.class), anyString(), anyList(), anyString());
    }
}