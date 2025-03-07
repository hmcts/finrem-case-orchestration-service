package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.List;

@Slf4j
@Service
public class SolicitorCreateContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final OnlineFormDocumentService service;
    private final FeatureToggleService featureToggleService;
    private final ExpressCaseService expressCaseService;
    private final CaseFlagsService caseFlagsService;
    private final IdamService idamService;
    private final UpdateRepresentationWorkflowService representationWorkflowService;

    private final CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator;

    @Autowired
    public SolicitorCreateContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        OnlineFormDocumentService service,
                                                        FeatureToggleService featureToggleService,
                                                        ExpressCaseService expressCaseService,
                                                        CaseFlagsService caseFlagsService,
                                                        IdamService idamService,
                                                        UpdateRepresentationWorkflowService representationWorkflowService,
                                                        CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator) {
        super(finremCaseDetailsMapper);
        this.service = service;
        this.expressCaseService = expressCaseService;
        this.featureToggleService = featureToggleService;
        this.caseFlagsService = caseFlagsService;
        this.idamService = idamService;
        this.representationWorkflowService = representationWorkflowService;
        this.createCaseMandatoryDataValidator = createCaseMandatoryDataValidator;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SOLICITOR_CREATE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callback, String authorisationToken) {
        log.info("Received request to generate draft Contested Mini Form A for Case ID : {}",
            callback.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        caseFlagsService.setCaseFlagInformation(caseDetails);

        FinremCaseData caseData = caseDetails.getData();

        List<String> mandatoryDataErrors = createCaseMandatoryDataValidator.validate(caseData);
        if (!mandatoryDataErrors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(mandatoryDataErrors)
                .data(caseData).build();
        }

        if (!idamService.isUserRoleAdmin(authorisationToken)) {
            log.info("other users for Case ID: {}", caseDetails.getId());
            caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        }
        CaseDocument document = service.generateDraftContestedMiniFormA(authorisationToken, caseDetails);
        caseData.setMiniFormA(document);

        representationWorkflowService.persistDefaultOrganisationPolicy(caseData);

        RefugeWrapperUtils.updateRespondentInRefugeTab(caseDetails);
        RefugeWrapperUtils.updateApplicantInRefugeTab(caseDetails);

        if (featureToggleService.isExpressPilotEnabled()) {
            expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }
}
