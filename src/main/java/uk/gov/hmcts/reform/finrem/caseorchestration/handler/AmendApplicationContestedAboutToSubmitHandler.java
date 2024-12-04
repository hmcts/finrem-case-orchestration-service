package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.CreateCaseMandatoryDataValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.List;

@Slf4j
@Service
public class AmendApplicationContestedAboutToSubmitHandler extends FinremCallbackHandler  {

    private static final String MIAM_INVALID_LEGACY_EXEMPTIONS_WARNING_MESSAGE =
        "The following MIAM exemptions are no longer valid and will be removed from the case data.";

    private final OnlineFormDocumentService onlineFormDocumentService;
    private final CaseFlagsService caseFlagsService;
    private final IdamService idamService;
    private final UpdateRepresentationWorkflowService representationWorkflowService;
    private final CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator;
    private final OnStartDefaultValueService onStartDefaultValueService;
    private final MiamLegacyExemptionsService miamLegacyExemptionsService;

    public AmendApplicationContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         OnlineFormDocumentService onlineFormDocumentService,
                                                         CaseFlagsService caseFlagsService,
                                                         IdamService idamService,
                                                         UpdateRepresentationWorkflowService representationWorkflowService,
                                                         CreateCaseMandatoryDataValidator createCaseMandatoryDataValidator,
                                                         OnStartDefaultValueService onStartDefaultValueService,
                                                         MiamLegacyExemptionsService miamLegacyExemptionsService) {
        super(finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.caseFlagsService = caseFlagsService;
        this.idamService = idamService;
        this.representationWorkflowService = representationWorkflowService;
        this.createCaseMandatoryDataValidator = createCaseMandatoryDataValidator;
        //keep the below?
        this.onStartDefaultValueService = onStartDefaultValueService;
        this.miamLegacyExemptionsService = miamLegacyExemptionsService;
    }

    // This is very WIP
    // Empty address strings from the create are changed to blank in this amend callback. Follow up.
    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String authorisationToken) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to submit callback for Case ID : {}",
                callbackRequest.getEventType(), caseDetails.getId());
        caseFlagsService.setCaseFlagInformation(caseDetails);
        FinremCaseData finremCaseData = caseDetails.getData();

        List<String> mandatoryDataErrors = createCaseMandatoryDataValidator.validate(finremCaseData);
        if (!mandatoryDataErrors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                    .errors(mandatoryDataErrors)
                    .data(finremCaseData).build();
        }

        if (!idamService.isUserRoleAdmin(authorisationToken)) {
            log.info("other users for Case ID: {}", caseDetails.getId());
            finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        }
        CaseDocument document = onlineFormDocumentService.generateDraftContestedMiniFormA(authorisationToken, caseDetails);
        finremCaseData.setMiniFormA(document);

        representationWorkflowService.persistDefaultOrganisationPolicy(finremCaseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(finremCaseData).build();
    }
}
