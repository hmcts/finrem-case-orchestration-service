package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

@Slf4j
@Service
public class SolicitorCreateConsentedAboutToSubmitHandler extends FinremCallbackHandler<FinremCaseDataConsented> {

    private final ConsentOrderService consentOrderService;
    private final IdamService idamService;
    private final CaseFlagsService caseFlagsService;

    public SolicitorCreateConsentedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 ConsentOrderService consentOrderService,
                                                 IdamService idamService,
                                                 CaseFlagsService caseFlagsService) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.idamService = idamService;
        this.caseFlagsService = caseFlagsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataConsented> handle(
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest, String userAuthorisation) {

        FinremCaseDetails<FinremCaseDataConsented> caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested event {} about to start callback for case id: {}",
            EventType.SOLICITOR_CREATE, caseDetails.getId());
        FinremCaseDataConsented caseData = caseDetails.getData();

        CaseDocument caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.setLatestConsentOrder(caseDocument);
        caseFlagsService.setCaseFlagInformation(callbackRequest.getCaseDetails());

        if (!idamService.isUserRoleAdmin(userAuthorisation)) {
            caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataConsented>builder()
            .data(caseData).build();
    }
}
