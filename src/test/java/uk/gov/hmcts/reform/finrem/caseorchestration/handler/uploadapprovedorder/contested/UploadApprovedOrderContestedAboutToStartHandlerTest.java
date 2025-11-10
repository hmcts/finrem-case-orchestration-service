package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedAboutToStartHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private PartyService partyService;

    @InjectMocks
    private UploadApprovedOrderContestedAboutToStartHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH);
    }

    @Test
    void givenUploadApprovedOrder_whenHandle_shouldInitialiseWorkingValues() {
        //Given
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.UPLOAD_APPROVED_ORDER);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        caseData.getManageHearingsWrapper().setIsFinalOrder(YesOrNo.YES);
        caseData.getManageHearingsWrapper().setIsAddHearingChosen(YesOrNo.YES);
        caseData.setOrderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        caseData.setOrderApprovedJudgeName("moj");
        caseData.setOrderApprovedDate(LocalDate.now());
        caseData.setHearingNoticeDocumentPack(List.of(
            DocumentCollectionItem.builder().value(caseDocument()).build()
        ));
        caseData.setUploadHearingOrder(List.of(
            DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(caseDocument())
                    .orderDateTime(LocalDateTime.now())
                    .isOrderStamped(YesOrNo.YES)
                    .build())
                .build()
        ));

        DynamicMultiSelectList partiesOnCase = mock(DynamicMultiSelectList.class);
        when(partyService.getAllActivePartyList(caseData))
            .thenReturn(partiesOnCase);

        //When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        //Assert
        FinremCaseData finremCaseData = response.getData();
        assertNull(finremCaseData.getManageHearingsWrapper().getIsFinalOrder());
        assertNull(finremCaseData.getManageHearingsWrapper().getIsAddHearingChosen());
        assertNull(finremCaseData.getOrderApprovedJudgeType());
        assertNull(finremCaseData.getOrderApprovedJudgeName());
        assertNull(finremCaseData.getOrderApprovedDate());
        assertThat(finremCaseData.getHearingNoticeDocumentPack()).isEmpty();
        assertThat(finremCaseData.getDraftDirectionWrapper().getCwApprovedOrderCollection())
            .contains(DirectionOrderCollection.EMPTY_COLLECTION);
        assertThat(finremCaseData.getManageHearingsWrapper().getWorkingHearing().getPartiesOnCaseMultiSelectList())
            .isEqualTo(partiesOnCase);
        assertFalse(response.hasErrors());
    }
}
