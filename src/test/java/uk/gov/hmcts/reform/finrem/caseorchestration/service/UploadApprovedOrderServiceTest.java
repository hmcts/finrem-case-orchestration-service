package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

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

    static Stream<Arguments> givenLastHearingDirectionRequiresAnotherHearing_whenProcessApprovedOrder_thenCreateHearingNoticeDocumentPack() {
        return Stream.of(
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(YesOrNo.YES)
                )
            ),
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(YesOrNo.NO),
                    buildHearingDirectionDetailsCollection(YesOrNo.YES)
                )
            ),
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(YesOrNo.YES),
                    buildHearingDirectionDetailsCollection(YesOrNo.YES)
                )
            ),
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(null),
                    buildHearingDirectionDetailsCollection(YesOrNo.YES)
                )
            )
        );
    }

    @SuppressWarnings("removal")
    @ParameterizedTest
    @MethodSource
    void givenLastHearingDirectionRequiresAnotherHearing_whenProcessApprovedOrder_thenCreateHearingNoticeDocumentPack(
        List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollections
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftDirectionWrapper(DraftDirectionWrapper.builder()
                .cwApprovedOrderCollection(List.of())
                .build())
            .hearingDirectionDetailsCollection(hearingDirectionDetailsCollections)
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        uploadApprovedOrderService.processApprovedOrders(callbackRequest, AUTH_TOKEN);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        InOrder inOrder = Mockito.inOrder(contestedOrderApprovedLetterService, approvedOrderNoticeOfHearingService,
            hearingOrderService);
        inOrder.verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).stampAndStoreCwApprovedOrders(finremCaseData, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        inOrder.verify(approvedOrderNoticeOfHearingService).createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertThat(finremCaseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()).isNull();
    }

    static Stream<Arguments> givenLastHearingDirectionDoesNotRequireAnotherHearing_whenProcessApprovedOrder_thenDoNotGenerateDocumentPack() {
        return Stream.of(
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(null)
                )
            ),
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(YesOrNo.NO)
                )
            ),
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(YesOrNo.YES),
                    buildHearingDirectionDetailsCollection(YesOrNo.NO)
                )
            ),
            Arguments.of(
                List.of(
                    buildHearingDirectionDetailsCollection(YesOrNo.NO),
                    buildHearingDirectionDetailsCollection(YesOrNo.NO)
                )
            )
        );
    }

    @SuppressWarnings("removal")
    @ParameterizedTest
    @MethodSource
    void givenLastHearingDirectionDoesNotRequireAnotherHearing_whenProcessApprovedOrder_thenDoNotGenerateDocumentPack(
        List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollections
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftDirectionWrapper(DraftDirectionWrapper.builder()
                .cwApprovedOrderCollection(List.of())
                .build())
            .hearingDirectionDetailsCollection(hearingDirectionDetailsCollections)
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        uploadApprovedOrderService.processApprovedOrders(callbackRequest, AUTH_TOKEN);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        InOrder inOrder = Mockito.inOrder(contestedOrderApprovedLetterService, approvedOrderNoticeOfHearingService,
            hearingOrderService);
        inOrder.verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).stampAndStoreCwApprovedOrders(finremCaseData, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        inOrder.verify(approvedOrderNoticeOfHearingService, never()).createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        assertThat(finremCaseData.getDraftDirectionWrapper().getCwApprovedOrderCollection()).isNull();
    }

    @Test
    void givenAnyCase_whenProcessApprovedOrderMh_thenGeneratesLetterStampsAndAppendsOrders() {
        List<DirectionOrderCollection> directionOrderCollections = mock(List.class);
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftDirectionWrapper(DraftDirectionWrapper.builder()
                .cwApprovedOrderCollection(directionOrderCollections)
                .build())
            .build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();

        uploadApprovedOrderService.processApprovedOrdersMh(finremCaseDetails, AUTH_TOKEN);

        InOrder inOrder = Mockito.inOrder(contestedOrderApprovedLetterService, hearingOrderService);
        inOrder.verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, AUTH_TOKEN);
        inOrder.verify(hearingOrderService).stampAndStoreCwApprovedOrders(finremCaseData, AUTH_TOKEN);
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

    private static HearingDirectionDetailsCollection buildHearingDirectionDetailsCollection(YesOrNo isAnotherHearingYN) {
        return HearingDirectionDetailsCollection.builder()
            .value(HearingDirectionDetail.builder().isAnotherHearingYN(isAnotherHearingYN).build())
            .build();
    }
}
