package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedAboutToStartHandlerTest {

    private UploadApprovedOrderContestedAboutToStartHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UploadApprovedOrderContestedAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper()));
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER);
    }

    @Test
    void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(EventType.UPLOAD_APPROVED_ORDER,
            FinremCaseDetailsBuilderFactory.from());
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        caseData.setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        caseData.setOrderApprovedJudgeName("moj");
        caseData.setOrderApprovedDate(LocalDate.now());
        List<DocumentCollectionItem> hearingNoticeDocumentPack = new ArrayList<>();
        DocumentCollectionItem collection = DocumentCollectionItem.builder().value(caseDocument()).build();
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
        assertThat(finremCaseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()).containsExactly(
            DirectionOrderCollection.EMPTY_COLLECTION
        );
        assertFalse(response.hasErrors());
    }
}
