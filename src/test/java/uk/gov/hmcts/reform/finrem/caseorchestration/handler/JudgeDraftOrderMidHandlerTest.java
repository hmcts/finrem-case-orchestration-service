package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class JudgeDraftOrderMidHandlerTest {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";

    private JudgeDraftOrderMidHandler handler;

    @Mock
    private BulkPrintDocumentService service;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new JudgeDraftOrderMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, MID_EVENT, CaseType.CONTESTED, EventType.JUDGE_DRAFT_ORDER);
    }

    @Test
    void givenMissingUploadedApprovedOrder_whenHandle_shouldGetError() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(EventType.JUDGE_DRAFT_ORDER, FinremCaseDetails.builder().data(FinremCaseData.builder().build()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        String errorMessage = "No orders have been uploaded. Please upload an order.";
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @Test
    void givenContestedCase_whenJudgeApprovedOrderUploaded_shouldValidateFileEncryption() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(EventType.JUDGE_DRAFT_ORDER,
                FinremCaseDetails.builder()
                    .data(FinremCaseData.builder().draftDirectionWrapper(DraftDirectionWrapper.builder()
                            .judgeApprovedOrderCollection(getDraftDirectionOrderObj())
                            .build())
                        .build()),
                FinremCaseDetails.builder()
                    .data(FinremCaseData.builder().draftDirectionWrapper(DraftDirectionWrapper.builder()
                            .judgeApprovedOrderCollection(getDraftDirectionOrderObj())
                            .build())
                        .build())
            );

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    private List<DraftDirectionOrderCollection> getDraftDirectionOrderObj() {
        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        DraftDirectionOrder directionOrder =
            DraftDirectionOrder
                .builder()
                .purposeOfDocument("test")
                .uploadDraftDocument(caseDocument)
                .build();
        DraftDirectionOrderCollection directionOrderCollection = DraftDirectionOrderCollection.builder().value(directionOrder).build();

        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = new ArrayList<>();
        draftDirectionOrderCollection.add(directionOrderCollection);
        return draftDirectionOrderCollection;
    }
}
