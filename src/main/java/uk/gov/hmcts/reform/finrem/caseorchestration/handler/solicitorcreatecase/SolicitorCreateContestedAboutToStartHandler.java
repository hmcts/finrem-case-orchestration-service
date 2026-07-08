package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
public class SolicitorCreateContestedAboutToStartHandler extends FinremCallbackHandler {

    private final OnStartDefaultValueService service;
    private final FeatureToggleService featureToggleService;

    public SolicitorCreateContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       OnStartDefaultValueService service,
                                                       FeatureToggleService featureToggleService) {
        super(finremCaseDetailsMapper);
        this.service = service;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        service.defaultCivilPartnershipField(callbackRequest);
        service.defaultTypeOfApplication(callbackRequest);
        service.defaultUrgencyQuestion(callbackRequest);
        setEstimatedAssetsChecklistVersion(callbackRequest);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }

    /**
     * This method sets the version of the Estimated Assets Checklist to be used in the case data based on a feature toggle.
     * If the feature toggle is enabled, it sets the version to V3; otherwise, it defaults to V2.
     *
     * @param callbackRequest The callback request containing the case data.
     */
    private void setEstimatedAssetsChecklistVersion(FinremCallbackRequest callbackRequest) {
        Boolean useV3EstimatedAssetsChecklist = featureToggleService.use_estimatedAssetsChecklistV3();
        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        if (useV3EstimatedAssetsChecklist) {
            caseData.getEstimatedAssetsChecklistWrapper().setEstimatedAssetsChecklistVersion(EstimatedAssetsChecklistVersion.V3);
        } else {
            caseData.getEstimatedAssetsChecklistWrapper().setEstimatedAssetsChecklistVersion(EstimatedAssetsChecklistVersion.V2);
        }
    }
}
