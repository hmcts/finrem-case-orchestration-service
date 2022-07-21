package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingAboutToSubmitHandler implements CallbackHandler {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ValidateHearingService validateHearingService;
    private final CaseDataService caseDataService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.LIST_FOR_HEARING.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for list for hearing for Case ID: {}", caseDetails.getId());

        final List<String> errors = validateHearingService.validateHearingErrors(caseDetails);
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        if (hearingDocumentService.alreadyHadFirstHearing(caseDetails)) {
            if (caseDataService.isContestedPaperApplication(caseDetails)) {
                additionalHearingDocumentService.createAdditionalHearingDocuments(userAuthorisation, caseDetails);
            }
        } else {
            caseDetails.getData().putAll(hearingDocumentService.generateHearingDocuments(userAuthorisation, caseDetails));
        }
        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData())
            .errors(ImmutableList.of())
            .warnings(warnings).build();
    }
}
