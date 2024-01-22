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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InterimHearingContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final InterimHearingService interimHearingService;

    public InterimHearingContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       InterimHearingService interimHearingService) {
        super(finremCaseDetailsMapper);
        this.interimHearingService = interimHearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("About to submit Interim hearing for Case ID {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = interimHearingService.getValidationErrors(caseData);

        if (!errors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(errors).build();
        }

        addNewInterimHearingsInOrderByDateToCase(caseData);

        interimHearingService.addHearingNoticesToCase(caseDetails,userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void addNewInterimHearingsInOrderByDateToCase(FinremCaseData caseData) {
        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        List<InterimHearingCollection> interimHearings =
            Optional.ofNullable(interimWrapper.getInterimHearings()).orElse(new ArrayList<>());

        interimHearings.addAll(interimWrapper.getInterimHearingsScreenField());
        interimWrapper.setInterimHearings(interimHearings.stream()
            .sorted(Comparator.nullsLast(Comparator.comparing(e -> e.getValue().getInterimHearingDate())))
            .collect(Collectors.toList()));
    }
}
