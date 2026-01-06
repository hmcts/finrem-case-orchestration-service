package uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CCDConcurrencyHelper {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;

    public StartEventResponse startEvent(CaseType caseType, Long caseId, String eventName) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getSysUserTokenUid();

        return coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            caseType.getCcdType(),
            caseId.toString(),
            eventName);
    }

    public void submitEvent(StartEventResponse startEventResponse, CaseType caseType, Long caseId, Map<String, Object> eventData) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getSysUserTokenUid();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(eventData)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            caseType.getCcdType(),
            caseId.toString(),
            true,
            caseDataContent);
    }
}
