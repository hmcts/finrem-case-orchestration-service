package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AmendApplicationContestedAboutToStartHandler extends FinremCallbackHandler  {

    private static final String MIAM_INVALID_LEGACY_EXEMPTIONS_WARNING_MESSAGE =
        "The following MIAM exemptions are no longer valid and will be removed from the case data.";

    private final OnStartDefaultValueService onStartDefaultValueService;
    private final MiamLegacyExemptionsService miamLegacyExemptionsService;

    private final AssignCaseAccessService assignCaseAccessService;

    public AmendApplicationContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        OnStartDefaultValueService onStartDefaultValueService,
                                                        MiamLegacyExemptionsService miamLegacyExemptionsService,
                                                        AssignCaseAccessService assignCaseAccessService) {
        super(finremCaseDetailsMapper);
        this.onStartDefaultValueService = onStartDefaultValueService;
        this.miamLegacyExemptionsService = miamLegacyExemptionsService;
        this.assignCaseAccessService = assignCaseAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.AMEND_CONTESTED_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Amend Application Details contested for Case ID : {}", callbackRequest.getCaseDetails().getId());

        onStartDefaultValueService.defaultCivilPartnershipField(callbackRequest);
        onStartDefaultValueService.defaultTypeOfApplication(callbackRequest);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        List<String> warnings = null;

        MiamWrapper miamWrapper = caseData.getMiamWrapper();
        if (miamLegacyExemptionsService.isLegacyExemptionsInvalid(miamWrapper)) {
            warnings = getMiamInvalidLegacyExemptionWarnings(miamWrapper);
        }
        miamLegacyExemptionsService.convertLegacyExemptions(miamWrapper);

        RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails);
        RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails);

        // setCurrentUserCaseRoleType so applicantInRefugeQuestion and respondentInRefugeQuestion labels show correctly.
        String loggedInUserCaseRole = assignCaseAccessService.getActiveUser(caseDetails.getCaseIdAsString(),
                userAuthorisation);
        caseData.setCurrentUserCaseRoleType(loggedInUserCaseRole);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(warnings)
            .build();
    }

    private List<String> getMiamInvalidLegacyExemptionWarnings(MiamWrapper miamWrapper) {
        List<String> invalidLegacyExemptions = miamLegacyExemptionsService.getInvalidLegacyExemptions(miamWrapper);

        List<String> warnings = new ArrayList<>();
        warnings.add(MIAM_INVALID_LEGACY_EXEMPTIONS_WARNING_MESSAGE);
        warnings.addAll(invalidLegacyExemptions);

        return warnings;
    }
}
