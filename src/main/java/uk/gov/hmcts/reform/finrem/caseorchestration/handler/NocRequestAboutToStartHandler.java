package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerRepresentationChecker;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@RequiredArgsConstructor
@Service
@Slf4j
public class NocRequestAboutToStartHandler implements CallbackHandler {

    private final IdamService idamService;
    private final IdamAuthService idamAuthService;
    private final BarristerRepresentationChecker barristerRepresentationChecker;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NOC_REQUEST.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {
        List<String> errors = newArrayList();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        log.info("About to start handling noc request barrister validation for case {}",
            callbackRequest.getCaseDetails().getId());

        String userId = idamService.getIdamUserId(userAuthorisation);
        UserDetails invokerDetails = idamAuthService.getUserByUserId(userAuthorisation, userId);
        log.info("User details for case {} :: {}", callbackRequest.getCaseDetails().getId(), invokerDetails.toString());

        if (barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseData, invokerDetails)) {
            log.info("User has represented litigant as Barrister for case {}", callbackRequest.getCaseDetails().getId());
            errors.add(String.format("User has represented litigant as Barrister for case %s",
                callbackRequest.getCaseDetails().getId()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build();
    }
}
