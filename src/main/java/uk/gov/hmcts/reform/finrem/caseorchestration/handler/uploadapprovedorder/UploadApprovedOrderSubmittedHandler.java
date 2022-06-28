package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApprovedOrderNoticeOfHearingService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadApprovedOrderSubmittedHandler implements CallbackHandler {

    private final ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (isAnotherHearingToBeListed(caseDetails)) {
            approvedOrderNoticeOfHearingService.printHearingNoticePackAndSendToApplicantAndRespondent(caseDetails, userAuthorisation);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }

    private boolean isAnotherHearingToBeListed(CaseDetails caseDetails) {
        Optional<AdditionalHearingDirectionsCollection> latestHearingDirections =
            getLatestAdditionalHearingDirections(caseDetails);
        return latestHearingDirections.isPresent()
            && YES_VALUE.equals(latestHearingDirections.get().getIsAnotherHearingYN());
    }

    private Optional<AdditionalHearingDirectionsCollection> getLatestAdditionalHearingDirections(CaseDetails caseDetails) {
        List<Element<AdditionalHearingDirectionsCollection>> additionalHearingDetailsCollection =
            new ObjectMapper().convertValue(caseDetails.getData().get(HEARING_DIRECTION_DETAILS_COLLECTION),
                new TypeReference<>() {});

        return additionalHearingDetailsCollection != null && !additionalHearingDetailsCollection.isEmpty()
            ? Optional.of(additionalHearingDetailsCollection.get(additionalHearingDetailsCollection.size() - 1).getValue())
            : Optional.empty();
    }
}
