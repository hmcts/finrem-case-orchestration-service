package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
@Slf4j
@Service
public class JudgeDraftOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final HearingOrderService hearingOrderService;                              
    private final GenericDocumentService genericDocumentService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final CaseDataService caseDataService;
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    public JudgeDraftOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingOrderService hearingOrderService, GenericDocumentService genericDocumentService,
                                               ContestedOrderApprovedLetterService contestedOrderApprovedLetterService,
                                               CaseDataService caseDataService,
                                               UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser) {
        super(finremCaseDetailsMapper);
        this.hearingOrderService = hearingOrderService;
        this.genericDocumentService = genericDocumentService;
        this.contestedOrderApprovedLetterService = contestedOrderApprovedLetterService;
        this.caseDataService = caseDataService;
        this.uploadedDraftOrderCategoriser = uploadedDraftOrderCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.JUDGE_DRAFT_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(finremCaseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}",
            EventType.JUDGE_DRAFT_ORDER, caseId);
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, userAuthorisation);
        convertAdditionalDocumentsToPdf(finremCaseDetails, userAuthorisation);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, userAuthorisation);
        caseDataService.moveCollection(caseData, DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);

        uploadedDraftOrderCategoriser.categorise(finremCaseDetails.getData());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseDetails.getData()).build();
    }

    private void convertAdditionalDocumentsToPdf(FinremCaseDetails caseDetails, String authorisation) {
        FinremCaseData caseData = caseDetails.getData();
        List<DraftDirectionOrderCollection> directionOrderCollection = caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection();

        directionOrderCollection.forEach(order -> order.getValue().getJudgeApprovedOrderAdditionalDocumentsCollection().forEach(additionalDoc -> {
            CaseDocument documentPdf = genericDocumentService.convertDocumentIfNotPdfAlready(
                additionalDoc.getValue(), authorisation,
                String.valueOf(caseDetails.getId()));

            additionalDoc.setValue(documentPdf);
        }));
    }
}
