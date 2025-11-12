package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction.ADD_HEARING;

@Slf4j
@Service
public class UploadApprovedOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentWarningsHelper documentWarningsHelper;
    private final UploadApprovedOrderService uploadApprovedOrderService;
    private final ManageHearingActionService manageHearingActionService;

    public UploadApprovedOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            DocumentWarningsHelper documentWarningsHelper,
                                                            UploadApprovedOrderService uploadApprovedOrderService,
                                                            ManageHearingActionService manageHearingActionService) {
        super(finremCaseDetailsMapper);
        this.documentWarningsHelper = documentWarningsHelper;
        this.uploadApprovedOrderService = uploadApprovedOrderService;
        this.manageHearingActionService = manageHearingActionService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        // Set the case ID so that requests to CDAM don't fail.
        // This is a temporary workaround until the request to CDAM is fixed to
        // send the case type ID and not the case ID. See DFR-4138
        caseData.setCcdCaseId(caseDetails.getCaseIdAsString());

        uploadApprovedOrderService.processApprovedOrdersMh(caseDetails, userAuthorisation);

        if (YesOrNo.YES.equals(caseData.getManageHearingsWrapper().getIsAddHearingChosen())) {
            ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();
            WorkingHearing workingHearing = manageHearingsWrapper.getWorkingHearing();

            manageHearingsWrapper.setManageHearingsActionSelection(ADD_HEARING);
            List<CaseDocument> uploadingHearingOrders = emptyIfNull(caseData.getDraftDirectionWrapper().getCwApprovedOrderCollection())
                .stream()
                .map(DirectionOrderCollection::getValue)
                .map(DirectionOrder::getUploadDraftDocument).toList();

            // Add order document to be included in hearing bulk print document bundle.
            // Ideally this doc would be added as a new document to Hearing Documents collection to avoid
            // the document appearing against the hearing when Edited. This requires completion of DFR-4040.
            uploadingHearingOrders.forEach(workingHearing::addDocumentToAdditionalHearingDocs);

            manageHearingActionService.performAddHearing(caseDetails, userAuthorisation);
            manageHearingActionService.updateTabData(caseData);
        }

        uploadApprovedOrderService.clearCwApprovedOrderCollection(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                emptyIfNull(data.getUploadHearingOrder()).stream()
                    .map(DirectionOrderCollection::getValue).toList(), userAuthorisation))
            .build();
    }
}
