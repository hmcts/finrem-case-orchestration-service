package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils;

import java.util.List;

@Slf4j
@Service
public class ConsentOrderInContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final CaseDataService caseDataService;

    private final OnlineFormDocumentService onlineFormDocumentService;

    private final DefaultsConfiguration defaultsConfiguration;

    private final DocumentWarningsHelper documentWarningsHelper;

    public ConsentOrderInContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       CaseDataService caseDataService,
                                                       OnlineFormDocumentService onlineFormDocumentService,
                                                       DefaultsConfiguration defaultsConfiguration,
                                                       DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.caseDataService = caseDataService;
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.defaultsConfiguration = defaultsConfiguration;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CONSENT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        final String caseId = String.valueOf(finremCaseDetails.getId());
        FinremCaseData caseData = finremCaseDetails.getData();

        // Logic moving from MiniFormAController.java
        if (!caseDataService.isConsentedInContestedCase(finremCaseDetails)) {
            CaseDocument miniFormA = onlineFormDocumentService.generateMiniFormA(userAuthorisation, caseDetails);
            caseData.setMiniFormA(miniFormA);

            log.info("Defaulting AssignedToJudge fields for Case ID: {}", caseId);
            populateAssignToJudgeFields(finremCaseDetails);
        } else {
            CaseDocument consentMiniFormA = onlineFormDocumentService.generateConsentedInContestedMiniFormA(
                caseDetails, userAuthorisation);
            caseData.getConsentOrderWrapper().setConsentMiniFormA(consentMiniFormA);
        }
        // END // Logic moving from MiniFormAController.java

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                    List.of(() -> ListUtils.safeListWithoutNulls(data.getConsentOrder())),
                userAuthorisation))
            .build();
    }

    // Cloning from MiniFormAController.populateAssignToJudgeFields
    private void populateAssignToJudgeFields(FinremCaseDetails finremCaseDetails) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        finremCaseData.setAssignedToJudge(defaultsConfiguration.getAssignedToJudgeDefault());
    }
}
