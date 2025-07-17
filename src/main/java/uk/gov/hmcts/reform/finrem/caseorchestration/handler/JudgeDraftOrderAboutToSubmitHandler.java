package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.List;

@Slf4j
@Service
public class JudgeDraftOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final HearingOrderService hearingOrderService;
    private final GenericDocumentService genericDocumentService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    public JudgeDraftOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingOrderService hearingOrderService,
                                               GenericDocumentService genericDocumentService,
                                               ContestedOrderApprovedLetterService contestedOrderApprovedLetterService,
                                               UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser) {
        super(finremCaseDetailsMapper);
        this.hearingOrderService = hearingOrderService;
        this.genericDocumentService = genericDocumentService;
        this.contestedOrderApprovedLetterService = contestedOrderApprovedLetterService;
        this.uploadedDraftOrderCategoriser = uploadedDraftOrderCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.JUDGE_DRAFT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        validateCaseData(callbackRequest);
        convertAdditionalDocumentsToPdf(finremCaseDetails, userAuthorisation);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, userAuthorisation);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, userAuthorisation);
        FinremCaseDetails finremCaseDetailsUpdated = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        uploadedDraftOrderCategoriser.categorise(finremCaseDetailsUpdated.getData());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseDetailsUpdated.getData()).build();
    }

    private void convertAdditionalDocumentsToPdf(FinremCaseDetails caseDetails, String authorisation) {
        FinremCaseData caseData = caseDetails.getData();
        List<DraftDirectionOrderCollection> directionOrderCollection = caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection();

        directionOrderCollection.stream().map(order -> order.getValue().getAdditionalDocuments())
            .filter(CollectionUtils::isNotEmpty).forEach(additionalDocs -> additionalDocs.forEach(additionalDoc -> {
                CaseDocument documentPdf = genericDocumentService.convertDocumentIfNotPdfAlready(
                    additionalDoc.getValue(), authorisation,
                    String.valueOf(caseDetails.getId()));

                additionalDoc.setValue(documentPdf);
            }));
    }
}
