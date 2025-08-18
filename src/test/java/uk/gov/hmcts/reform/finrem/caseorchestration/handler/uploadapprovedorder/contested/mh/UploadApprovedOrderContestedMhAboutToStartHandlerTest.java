package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.mh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.UploadApprovedOrderContestedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedMhAboutToStartHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private PartyService partyService;

    @InjectMocks
    private UploadApprovedOrderContestedAboutToStartHandler handler;

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH));
    }

    @Test
    void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.UPLOAD_APPROVED_ORDER);
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

        DynamicMultiSelectList partiesOnCase = DynamicMultiSelectList
            .builder()
            .listItems(List.of(
                DynamicMultiSelectListElement
                    .builder()
                    .code("party1")
                    .label("Party 1")
                    .build(),
                DynamicMultiSelectListElement
                    .builder()
                    .code("party2")
                    .label("Party 2")
                    .build()))
            .build();

        when(partyService.getAllActivePartyList(caseData))
            .thenReturn(partiesOnCase);

        var response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        FinremCaseData finremCaseData = response.getData();
        assertNull(finremCaseData.getOrderApprovedJudgeType());
        assertNull(finremCaseData.getOrderApprovedJudgeName());
        assertNull(finremCaseData.getOrderApprovedDate());
        assertThat(finremCaseData.getHearingNoticeDocumentPack()).isEmpty();
        assertTrue(finremCaseData.getUploadHearingOrder().isEmpty());
        assertThat(finremCaseData.getManageHearingsWrapper().getWorkingHearing().getPartiesOnCaseMultiSelectList())
            .isEqualTo(partiesOnCase);
        assertFalse(response.hasErrors());
    }
}
