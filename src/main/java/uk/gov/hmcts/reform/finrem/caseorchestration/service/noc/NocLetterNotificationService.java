package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.LetterHandler;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NocLetterNotificationService {

    private final List<LetterHandler> letterHandlers;

    public void sendNoticeOfChangeLetters(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authToken) {
        log.info("Call the noc letter handlers for case id {}", caseDetails.getId());
        letterHandlers.stream().forEach(letterHandler -> letterHandler.handle(caseDetails, caseDetailsBefore, authToken));
    }
}
