package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.LetterHandler;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NocLetterNotificationService {

    private final List<LetterHandler> letterHandlers;

    /**
     * Invokes all configured Notice of Change (NOC) letter handlers for the given case.
     *
     * <p>This method iterates through the registered {@code letterHandlers} and delegates
     * the handling of NOC letter generation and sending to each handler. Each handler is
     * responsible for determining whether a letter should be produced based on the
     * provided case details and performing the appropriate action.
     *
     * <p><strong>Note:</strong> This implementation should be refactored in the future to
     * use {@link SendCorrespondenceEvent} for consistency with the notification
     * approach.
     *
     * @param caseDetails       the current case details
     * @param caseDetailsBefore the previous case details, used to detect changes
     * @param authToken         the authorisation token used for downstream service calls
     */
    public void sendNoticeOfChangeLetters(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authToken) {
        log.info("Call the noc letter handlers for Case ID: {}", caseDetails.getId());
        letterHandlers.forEach(letterHandler -> letterHandler.handle(caseDetails, caseDetailsBefore, authToken));
    }
}
