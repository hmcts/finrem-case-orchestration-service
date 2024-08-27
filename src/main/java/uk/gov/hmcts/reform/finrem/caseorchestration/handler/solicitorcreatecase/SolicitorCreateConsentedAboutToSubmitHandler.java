package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.CreateCaseMandatoryDataValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.List;

@Slf4j
@Service
public class SolicitorCreateConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderService consentOrderService;
    private final IdamService idamService;
    private final CaseFlagsService caseFlagsService;
    private final CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator;
    private final UpdateRepresentationWorkflowService representationWorkflowService;

    public SolicitorCreateConsentedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        ConsentOrderService consentOrderService,
                                                        IdamService idamService,
                                                        CaseFlagsService caseFlagsService,
                                                        CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator,
                                                        UpdateRepresentationWorkflowService representationWorkflowService) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.idamService = idamService;
        this.caseFlagsService = caseFlagsService;
        this.createCaseMandatoryDataValidator = createCaseMandatoryDataValidator;
        this.representationWorkflowService = representationWorkflowService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested event {} about to start callback for Case ID: {}",
            EventType.SOLICITOR_CREATE, caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        List<String> mandatoryDataErrors = createCaseMandatoryDataValidator.validate(caseData);
        if (!mandatoryDataErrors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(mandatoryDataErrors)
                .data(caseData).build();
        }

        CaseDocument caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.setLatestConsentOrder(caseDocument);
        caseFlagsService.setCaseFlagInformation(callbackRequest.getCaseDetails());

        if (!idamService.isUserRoleAdmin(userAuthorisation)) {
            caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        }

        representationWorkflowService.persistDefaultOrganisationPolicy(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }
}
