package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ManageHearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;

@RequiredArgsConstructor
@Service
@Slf4j
public class ManageHearingsCorresponder {

    private final HearingNotificationHelper hearingNotificationHelper;

    // todo
    public void sendHearingNotifications(FinremCallbackRequest callbackRequest) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Hearing hearing = hearingNotificationHelper.getHearingInContext(finremCaseData);

        if (hearingNotificationHelper.shouldSendNotification(hearing)) {
            hearing.getPartiesOnCaseMultiSelectList()
                    .getValue()
                    .forEach(party -> hearingNotificationHelper.sendHearingNotificationsByParty(
                            party,
                            finremCaseDetails,
                            hearing));
        }
    }
}
