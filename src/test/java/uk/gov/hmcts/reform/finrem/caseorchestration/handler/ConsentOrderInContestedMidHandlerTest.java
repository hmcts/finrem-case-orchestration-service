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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ConsentOrderInContestedMidHandlerTest {

    private ConsentOrderInContestedMidHandler handler;
    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new ConsentOrderInContestedMidHandler(finremCaseDetailsMapper, bulkPrintDocumentService);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CONSENT_ORDER);
    }

    @Test
    void handle_shouldValidateEncryptionOnEssentialFilesPlusNewlyUploadedOtherDocument() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        setupConsentOtherDocumentCollection(finremCallbackRequest);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verifyValidateEncryptionOnUploadedDocument(6); // essential validations + 1 newly uploaded other documents
    }

    // The following essential documents must be validated, so the expected time is 5.
    // consentOrder, consentD81Joint, consentD81Applicant, consentD81Respondent, consentVariationOrderDocument
    @Test
    void handle_shouldAlwaysValidateEncryptionOnFiveEssentialFiles() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        setupConsentOtherDocumentCollection(finremCallbackRequest);

        FinremCaseData before = finremCallbackRequest.getCaseDetailsBefore().getData();
        FinremCaseData after = finremCallbackRequest.getCaseDetails().getData();

        // 1. validation should not be affected by any documents in consentOtherCollection
        ConsentOrderWrapper afterConsentOrderWrapper = after.getConsentOrderWrapper();
        when(before.getConsentOrderWrapper()).thenReturn(afterConsentOrderWrapper);

        // 2. validation should not be affected by any documents in consentPensionCollection
        PensionTypeCollection typeCollection = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder()
                .typeOfDocument(PensionDocumentType.FORM_P1)
                .pensionDocument(caseDocument("pensionDocument1.pdf"))
                .build())
            .build();

        when(after.getConsentPensionCollection()).thenReturn(new ArrayList<>(List.of(typeCollection)));
        when(before.getConsentPensionCollection()).thenReturn(new ArrayList<>(List.of(typeCollection)));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verifyValidateEncryptionOnUploadedDocument(5);
    }

    @Test
    void handle_shouldValidateEssentialFilesPlusNewlyAddedPensionDocument() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        setupConsentOtherDocumentCollection(finremCallbackRequest);

        FinremCaseData before = finremCallbackRequest.getCaseDetailsBefore().getData();
        FinremCaseData after = finremCallbackRequest.getCaseDetails().getData();

        ConsentOrderWrapper afterConsentOrderWrapper = after.getConsentOrderWrapper();
        when(before.getConsentOrderWrapper()).thenReturn(afterConsentOrderWrapper);

        // 2. newly added pension collection
        PensionTypeCollection typeCollection = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder()
                .typeOfDocument(PensionDocumentType.FORM_P1)
                .pensionDocument(caseDocument("pensionDocument.pdf"))
                .build())
            .build();
        PensionTypeCollection newlyAddedPensionTypeCollection = PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder()
                .typeOfDocument(PensionDocumentType.FORM_P1)
                .pensionDocument(caseDocument("newPensionDocument.pdf"))
                .build())
            .build();

        when(after.getConsentPensionCollection()).thenReturn(new ArrayList<>(List.of(typeCollection, newlyAddedPensionTypeCollection)));
        when(before.getConsentPensionCollection()).thenReturn(new ArrayList<>(List.of(typeCollection)));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verifyValidateEncryptionOnUploadedDocument(6);
    }

    @Test
    void handle_shouldValidateEssentialFilesPlusNewlyAddedOtherDocument() {
        FinremCallbackRequest finremCallbackRequest = buildBaseCallbackRequest();
        setupConsentOtherDocumentCollection(finremCallbackRequest);

        FinremCaseData before = finremCallbackRequest.getCaseDetailsBefore().getData();

        ConsentOrderWrapper beforeConsentOrderWrapper = mockConsentOrderWrapper();
        // Set the existing 'other collection' to empty to indicate that a new one is being added.
        when(beforeConsentOrderWrapper.getConsentOtherCollection()).thenReturn(List.of());
        when(before.getConsentOrderWrapper()).thenReturn(beforeConsentOrderWrapper);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verifyValidateEncryptionOnUploadedDocument(6);
    }

    private FinremCallbackRequest buildBaseCallbackRequest() {
        FinremCallbackRequest mockedCallbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails mockedCaseDetails = mock(FinremCaseDetails.class);
        when(mockedCallbackRequest.getCaseDetails()).thenReturn(mockedCaseDetails);

        ConsentOrderWrapper mockedConsentOrderWrapper = mockConsentOrderWrapper();

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseDetails.getData()).thenReturn(mockedCaseData);
        when(mockedCaseDetails.getData().getConsentOrder()).thenReturn(caseDocument("consentOrder.pdf"));
        when(mockedCaseDetails.getData().getConsentOrderWrapper()).thenReturn(mockedConsentOrderWrapper);
        when(mockedCaseDetails.getData().getConsentVariationOrderDocument()).thenReturn(caseDocument("VariationOrderDocument.pdf"));

        // Setting up Before FinremCaseDetails
        FinremCaseData mockedCaseDataBefore = mock(FinremCaseData.class);
        FinremCaseDetails mockedCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(mockedCallbackRequest.getCaseDetailsBefore()).thenReturn(mockedCaseDetailsBefore);
        when(mockedCaseDetailsBefore.getData()).thenReturn(mockedCaseDataBefore);

        return mockedCallbackRequest;
    }

    private ConsentOrderWrapper mockConsentOrderWrapper() {
        final CaseDocument d18Joint = caseDocument("d18Joint.pdf");
        final CaseDocument d18Applicant = caseDocument("d18Applicant.pdf");
        final CaseDocument d18Respondent = caseDocument("d18Respondent.pdf");

        ConsentOrderWrapper mockedConsentOrderWrapper = mock(ConsentOrderWrapper.class);
        lenient().when(mockedConsentOrderWrapper.getConsentD81Joint()).thenReturn(d18Joint);
        lenient().when(mockedConsentOrderWrapper.getConsentD81Applicant()).thenReturn(d18Applicant);
        lenient().when(mockedConsentOrderWrapper.getConsentD81Respondent()).thenReturn(d18Respondent);
        return mockedConsentOrderWrapper;
    }

    private void verifyValidateEncryptionOnUploadedDocument(int times) {
        verify(bulkPrintDocumentService, times(times))
            .validateEncryptionOnUploadedDocument(any(CaseDocument.class), anyString(), anyList(), anyString());
    }

    private void setupConsentOtherDocumentCollection(FinremCallbackRequest finremCallbackRequest) {
        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        // not affected by consentOtherCollection
        when(finremCaseData.getConsentOrderWrapper().getConsentOtherCollection()).thenReturn(new ArrayList<>(
            List.of(
                OtherDocumentCollection.builder()
                    .value(OtherDocument.builder()
                        .uploadedDocument(caseDocument("otherDocument1.pdf"))
                        .build())
                    .build()
            )
        ));
    }
}
