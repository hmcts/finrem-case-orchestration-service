package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.PROCESS_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;

@Slf4j
@Service
public class ProcessOrdersAboutToStartHandler extends FinremCallbackHandler {

    private final HasApprovableCollectionReader hasApprovableCollectionReader;

    public ProcessOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                            HasApprovableCollectionReader hasApprovableCollectionReader) {
        super(finremCaseDetailsMapper);
        this.hasApprovableCollectionReader = hasApprovableCollectionReader;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType) && CONTESTED.equals(caseType) && PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} about to start callback for Case ID: {}", EventType.DIRECTION_UPLOAD_ORDER, caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<String> errors = new ArrayList<>();

        populateUnprocessedApprovedDocuments(caseData);
        populateMetaDataFields(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void populateMetaDataFields(FinremCaseData caseData) {
        caseData.getDraftOrdersWrapper().setIsLegacyApprovedOrderPresent(YesOrNo.forValue(!ofNullable(caseData.getUploadHearingOrder())
            .orElse(List.of()).isEmpty()));
        caseData.getDraftOrdersWrapper().setIsUnprocessedApprovedDocumentPresent(YesOrNo.forValue(!ofNullable(caseData.getDraftOrdersWrapper()
            .getUnprocessedApprovedDocuments()).orElse(List.of()).isEmpty()));
    }

    private void populateUnprocessedApprovedDocuments(FinremCaseData caseData) {
        DraftOrdersWrapper draftOrdersWrapper = caseData.getDraftOrdersWrapper();

        List<DraftOrderDocReviewCollection> draftOrderCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectDraftOrderDocs(draftOrdersWrapper.getDraftOrdersReviewCollection(),
            draftOrderCollector, APPROVED_BY_JUDGE::equals);
        List<PsaDocReviewCollection> psaCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectPsaDocs(draftOrdersWrapper.getDraftOrdersReviewCollection(),
            psaCollector, APPROVED_BY_JUDGE::equals);

        Function<HasApprovable, DirectionOrderCollection> directionOrderCollectionConvertor = d -> DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .isOrderStamped(YesOrNo.NO) // It's not stamped in the new draft order flow
                .orderDateTime(d.getValue().getApprovalDate())
                .uploadDraftDocument(d.getValue().getTargetDocument())
                .build())
            .build();

        List<DirectionOrderCollection> result = new ArrayList<>(draftOrderCollector.stream()
            .map(directionOrderCollectionConvertor).toList());
        result.addAll(psaCollector.stream()
            .map(directionOrderCollectionConvertor).toList());
        caseData.getDraftOrdersWrapper().setUnprocessedApprovedDocuments(result);
    }

}
