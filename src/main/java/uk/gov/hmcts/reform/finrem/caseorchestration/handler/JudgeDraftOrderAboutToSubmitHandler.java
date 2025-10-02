package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class JudgeDraftOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final HearingOrderService hearingOrderService;
    private final GenericDocumentService genericDocumentService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;
    private final DocumentWarningsHelper documentWarningsHelper;

    public JudgeDraftOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingOrderService hearingOrderService,
                                               GenericDocumentService genericDocumentService,
                                               ContestedOrderApprovedLetterService contestedOrderApprovedLetterService,
                                               UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser,
                                               DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.hearingOrderService = hearingOrderService;
        this.genericDocumentService = genericDocumentService;
        this.contestedOrderApprovedLetterService = contestedOrderApprovedLetterService;
        this.uploadedDraftOrderCategoriser = uploadedDraftOrderCategoriser;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.JUDGE_DRAFT_ORDER.equals(eventType); // Event: Upload approved order
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        validateCaseData(callbackRequest);
        // Set the case ID so that requests to CDAM don't fail.
        // This is a temporary workaround until the request to CDAM is fixed to
        // send the case type ID and not the case ID. See DFR-4138
        finremCaseData.setCcdCaseId(String.valueOf(finremCaseDetails.getId()));
        convertAdditionalDocumentsToPdf(finremCaseDetails, userAuthorisation);

        hearingOrderService.stampAndStoreJudgeApprovedOrders(finremCaseData, userAuthorisation);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, userAuthorisation);
        uploadedDraftOrderCategoriser.categorise(finremCaseData);
        moveJudgeUploadedApprovedOrdersToDraftDirectionOrderCollection(finremCaseData);
        clearJudgeApprovedOrderCollection(finremCaseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                emptyIfNull(data.getDraftDirectionWrapper().getDraftDirectionOrderCollection()).stream()
                    .map(DraftDirectionOrderCollection::getValue).toList(), userAuthorisation))
            .build();
    }

    private void convertAdditionalDocumentsToPdf(FinremCaseDetails caseDetails, String authorisation) {
        FinremCaseData caseData = caseDetails.getData();
        List<DraftDirectionOrderCollection> judgeApprovedOrderCollection = caseData.getDraftDirectionWrapper().getJudgeApprovedOrderCollection();

        emptyIfNull(judgeApprovedOrderCollection).stream()
            .map(DraftDirectionOrderCollection::getValue)
            .map(DraftDirectionOrder::getAdditionalDocuments)
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(List::stream)
            .forEach(additionalDoc -> {
                CaseDocument documentPdf = genericDocumentService.convertDocumentIfNotPdfAlready(
                    additionalDoc.getValue(), authorisation, String.valueOf(caseDetails.getId()));
                additionalDoc.setValue(documentPdf);
            });
    }

    /**
     * Moves the judge-uploaded approved orders to the draft direction order collection.
     * <p>
     * This method transfers the uploaded original copies (raw) from the judge-approved
     * order collection to the {@code draftDirectionOrderCollection}, which is displayed
     * under the "Upload approved order" section in the Case Documents tab.
     * <p>
     * If the {@code draftDirectionOrderCollection} is {@code null}, it will be
     * initialised as an empty list before adding the orders.
     *
     * @param finremCaseData the case data containing the draft direction wrapper and
     *                       the judge-approved order collection
     */
    private void moveJudgeUploadedApprovedOrdersToDraftDirectionOrderCollection(FinremCaseData finremCaseData) {
        DraftDirectionWrapper draftDirectionWrapper = finremCaseData.getDraftDirectionWrapper();
        if (draftDirectionWrapper.getDraftDirectionOrderCollection() == null) {
            draftDirectionWrapper.setDraftDirectionOrderCollection(new ArrayList<>());
        }
        draftDirectionWrapper.getDraftDirectionOrderCollection().addAll(
            emptyIfNull(draftDirectionWrapper.getJudgeApprovedOrderCollection()
        ));
    }

    private void clearJudgeApprovedOrderCollection(FinremCaseData finremCaseData) {
        finremCaseData.getDraftDirectionWrapper().setJudgeApprovedOrderCollection(null);
    }
}
