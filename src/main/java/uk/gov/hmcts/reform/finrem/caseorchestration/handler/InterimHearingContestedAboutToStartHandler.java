package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class InterimHearingContestedAboutToStartHandler extends FinremCallbackHandler {

    private final PartyService partyService;
    private final InterimHearingService interimHearingService;

    public InterimHearingContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      PartyService partyService,
                                                      InterimHearingService interimHearingService) {
        super(finremCaseDetailsMapper);
        this.partyService = partyService;
        this.interimHearingService = interimHearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequestWithFinremCaseDetails.getCaseDetails();
        log.info("In Interim hearing about to start callback for Case ID: {}", caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();

        Optional.ofNullable(
                finremCaseData.getInterimWrapper().getInterimHearingsScreenField())
            .orElse(new ArrayList<>()).clear();
        Optional.ofNullable(
                finremCaseData.getInterimWrapper().getInterimHearingDocuments())
            .orElse(new ArrayList<>()).clear();

        migrateLegacyFields(finremCaseData);

        DynamicMultiSelectList allActivePartyList = partyService.getAllActivePartyList(caseDetails);
        finremCaseData.setPartiesOnCase(allActivePartyList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

    private void migrateLegacyFields(FinremCaseData finremCaseData) {
        InterimWrapper interimWrapper = finremCaseData.getInterimWrapper();
        if (interimWrapper.getInterimHearingType() != null) {
            interimWrapper.setInterimHearings(
                interimHearingService.getLegacyInterimHearingAsInterimHearingCollection(finremCaseData));
        }

        interimHearingService.clearLegacyInterimData(finremCaseData);
    }
}
