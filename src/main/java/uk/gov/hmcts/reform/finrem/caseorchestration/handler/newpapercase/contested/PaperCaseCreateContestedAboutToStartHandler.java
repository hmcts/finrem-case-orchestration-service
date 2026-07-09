package uk.gov.hmcts.reform.finrem.caseorchestration.handler.newpapercase.contested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy.getDefaultOrganisationPolicy;

@Slf4j
@Service
public class PaperCaseCreateContestedAboutToStartHandler extends FinremCallbackHandler {

    private final OnStartDefaultValueService onStartDefaultValueService;

    public PaperCaseCreateContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       OnStartDefaultValueService onStartDefaultValueService) {
        super(finremCaseDetailsMapper);
        this.onStartDefaultValueService = onStartDefaultValueService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NEW_PAPER_CASE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        validateCaseData(callbackRequest);
        setOrganisationPolicyForNewPaperCase(finremCaseData);

        onStartDefaultValueService.defaultCivilPartnershipField(callbackRequest);
        onStartDefaultValueService.defaultTypeOfApplication(callbackRequest);
        onStartDefaultValueService.defaultUrgencyQuestion(callbackRequest);

        return response(finremCaseData);
    }

    private void setOrganisationPolicyForNewPaperCase(FinremCaseData finremCaseData) {
        finremCaseData.setApplicantOrganisationPolicy(
            getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR)
        );
        finremCaseData.setRespondentOrganisationPolicy(
            getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR)
        );
    }
}
