package uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HwfNotificationsService {

    private final List<HwfNotificationsHandler> handlers;

    public void sendNotification(CaseDetails caseDetails, String authToken) {
        handlers.stream()
            .filter(handler -> handler.canHandle(caseDetails))
            .forEach(handler -> handler.sendNotification(caseDetails, authToken));
    }

}
