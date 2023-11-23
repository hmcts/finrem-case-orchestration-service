package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedAboutToStartHandlerTest extends BaseHandlerTestSetup {

    private static final String AUTH_TOKEN = "Token:-)";
    private UploadApprovedOrderContestedAboutToStartHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UploadApprovedOrderContestedAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper()));
    }

    @Test
    void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER));
    }

    @Test
    void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCannotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE));
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER));
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.UPLOAD_APPROVED_ORDER);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        caseData.setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        caseData.setOrderApprovedJudgeName("moj");
        caseData.setOrderApprovedDate(LocalDate.now());
        List<DocumentCollection> hearingNoticeDocumentPack = new ArrayList<>();
        DocumentCollection collection = DocumentCollection.builder().value(caseDocument()).build();
        hearingNoticeDocumentPack.add(collection);
        caseData.setHearingNoticeDocumentPack(hearingNoticeDocumentPack);

        List<DirectionOrderCollection> uploadHearingOrder = new ArrayList<>();
        DirectionOrder directionOrder = DirectionOrder.builder().uploadDraftDocument(caseDocument())
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(directionOrder).build();
        uploadHearingOrder.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrder);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        FinremCaseData finremCaseData = response.getData();
        assertNull(finremCaseData.getOrderApprovedJudgeType());
        assertNull(finremCaseData.getOrderApprovedJudgeName());
        assertNull(finremCaseData.getOrderApprovedDate());
        assertTrue(finremCaseData.getHearingNoticeDocumentPack().isEmpty());
        assertTrue(finremCaseData.getUploadHearingOrder().isEmpty());
        assertFalse(response.hasErrors());
    }
}
