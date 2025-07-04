package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;

@RequiredArgsConstructor
@Service
@Slf4j
public class ManageHearingsCorresponder {

    private final HearingNotificationHelper hearingNotificationHelper;

    /**
     * Sends hearing notifications to relevant parties based on the callback request.
     * Loops through each selected party in the hearing and sends using
     * {@link HearingNotificationHelper#sendHearingNotificationsByParty}.</p>
     *
     * @param callbackRequest the callback request containing case and hearing data
     */
    public void sendHearingNotifications(FinremCallbackRequest callbackRequest) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Hearing hearing = hearingNotificationHelper.getHearingInContext(finremCaseData);

        if (!hearingNotificationHelper.shouldSendNotification(hearing)) {
            return;
        }

        DynamicMultiSelectList partyList = hearing.getPartiesOnCaseMultiSelectList();
        if (partyList == null || partyList.getValue() == null) {
            return;
        }

        for (DynamicMultiSelectListElement party : partyList.getValue()) {
            hearingNotificationHelper.sendHearingNotificationsByParty(
                    party,
                    finremCaseDetails,
                    hearing
            );
        }
    }
}
