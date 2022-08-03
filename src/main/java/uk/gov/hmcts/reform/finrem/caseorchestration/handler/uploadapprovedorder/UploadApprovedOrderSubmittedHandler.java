package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApprovedOrderNoticeOfHearingService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetailsCollection;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.isYes;

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
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (isAnotherHearingToBeListed(caseDetails)) {
            approvedOrderNoticeOfHearingService.printHearingNoticePackAndSendToApplicantAndRespondent(caseDetails, userAuthorisation);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build();
    }

    private boolean isAnotherHearingToBeListed(FinremCaseDetails caseDetails) {
        Optional<HearingDirectionDetail> latestHearingDirections =
            getLatestAdditionalHearingDirections(caseDetails);
        return latestHearingDirections.isPresent() && isYes(latestHearingDirections.get().getIsAnotherHearingYN());
    }

    private Optional<HearingDirectionDetail> getLatestAdditionalHearingDirections(FinremCaseDetails caseDetails) {
        List<HearingDirectionDetailsCollection> additionalHearingDetailsCollection = caseDetails.getCaseData()
            .getHearingDirectionDetailsCollection();

        return !CollectionUtils.isEmpty(additionalHearingDetailsCollection)
            ? Optional.of(Iterables.getLast(additionalHearingDetailsCollection).getValue())
            : Optional.empty();
    }
}
