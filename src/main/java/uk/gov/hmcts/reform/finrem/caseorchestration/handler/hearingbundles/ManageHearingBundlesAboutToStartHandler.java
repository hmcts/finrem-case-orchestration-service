package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ManageHearingBundlesAboutToStartHandler extends FinremCallbackHandler {

    private HearingDatePopulatedValidator hearingDatePopulatedValidator;

    public ManageHearingBundlesAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                   HearingDatePopulatedValidator hearingDatePopulatedValidator) {
        super(finremCaseDetailsMapper);
        this.hearingDatePopulatedValidator = hearingDatePopulatedValidator;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_HEARING_BUNDLES.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling manage hearing bundles about to start callback for case id: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (caseDetails.getData().getHearingUploadBundle() == null) {
            caseDetails.getData().setHearingUploadBundle(new ArrayList<>());
        }
        if (caseDetails.getData().getFdrHearingBundleCollections() != null) {
            caseDetails.getData().getHearingUploadBundle().addAll(caseDetails.getData().getFdrHearingBundleCollections());
        }

        List<String> errors = hearingDatePopulatedValidator.validateHearingDate(caseDetails.getData());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData())
            .errors(errors)
            .build();
    }


}
