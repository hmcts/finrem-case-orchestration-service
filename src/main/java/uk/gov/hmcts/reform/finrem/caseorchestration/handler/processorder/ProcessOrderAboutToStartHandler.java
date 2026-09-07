package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class ProcessOrderAboutToStartHandler extends FinremCallbackHandler {

    private final ProcessOrderService processOrderService;
    private final PartyService partyService;

    public ProcessOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                           ProcessOrderService processOrderService, PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.processOrderService = processOrderService;
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType)
            && CONTESTED.equals(caseType)
            && EventType.PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        processOrderService.populateUnprocessedUploadHearingDocuments(caseData);
        processOrderService.populateUnprocessedApprovedDocuments(caseData);
        populateMetaDataFields(caseData);

        List<String> errors = new ArrayList<>();
        if (processOrderService.hasNoApprovedOrdersToProcess(caseData)) {
            String error = "There are no draft orders to be processed.";
            errors.add(error);
            return response(caseData, null, errors);
        }

        // Initialise Working Hearings if the event is PROCESS_ORDER
        if (EventType.PROCESS_ORDER.equals(callbackRequest.getEventType())) {
            ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();
            manageHearingsWrapper.setIsAddHearingChosen(null);
            manageHearingsWrapper.setWorkingHearing(
                WorkingHearing.builder()
                    .partiesOnCaseMultiSelectList(partyService.getAllActivePartyList(caseDetails))
                    .withHearingTypes(HearingType.values())
                    .build()
            );
        }

        return response(caseData, null, errors);
    }

    private void populateMetaDataFields(FinremCaseData caseData) {
        caseData.getDraftOrdersWrapper().setIsLegacyApprovedOrderPresent(YesOrNo.forValue(!CollectionUtils
            .isEmpty(caseData.getUnprocessedUploadHearingDocuments())));
        caseData.getDraftOrdersWrapper().setIsUnprocessedApprovedDocumentPresent(YesOrNo.forValue(!CollectionUtils
            .isEmpty(caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments())));
    }
}
