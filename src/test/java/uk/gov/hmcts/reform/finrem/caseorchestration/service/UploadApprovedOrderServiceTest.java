package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderServiceTest  {

    @InjectMocks
    private UploadApprovedOrderService uploadApprovedOrderService;
    @Mock
    private HearingOrderService hearingOrderService;
    @Mock
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @Mock
    private ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    @Test
    void givenAnyCase_whenProcessApprovedOrderMh_thenGeneratesLetterStampsAndAppendsOrders() {
        List<DirectionOrderCollection> directionOrderCollections = mock(List.class);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftDirectionWrapper(DraftDirectionWrapper.builder()
                .cwApprovedOrderCollection(directionOrderCollections)
                .build())
            .build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails
            .builder()
            .caseType(CONTESTED)
            .data(finremCaseData).build();

        uploadApprovedOrderService.processApprovedOrdersMh(finremCaseDetails, AUTH_TOKEN);

        InOrder inOrder = Mockito.inOrder(contestedOrderApprovedLetterService, hearingOrderService);
        inOrder.verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).stampAndStoreCwApprovedOrders(finremCaseDetails, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(finremCaseDetails);
        verifyNoInteractions(approvedOrderNoticeOfHearingService);

        assertThat(finremCaseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()).isEqualTo(directionOrderCollections);
    }

    @Test
    void givenAnyCase_whenClearCwApprovedOrderCollection_thenClearCwApprovedOrderCollection() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftDirectionWrapper(DraftDirectionWrapper.builder()
                .cwApprovedOrderCollection(List.of())
                .build())
            .build();
        uploadApprovedOrderService.clearCwApprovedOrderCollection(finremCaseData);
        assertThat(finremCaseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()).isNull();
    }
}
