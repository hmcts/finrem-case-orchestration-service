package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.wrapper.IdamToken;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdService {

    public static final String CCD_EVENT_DESCRIPTION = "Updated case state through financial remedy services";
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;

    private final Map<String, Map<String, Object>> supplementaryDataRequestMap = new HashMap<>() {
        {
            put("$set", hmctsServiceIdMap);
        }
    };

    private final Map<String, Object> hmctsServiceIdMap = new HashMap<>() {
        {
            put("HMCTSServiceId", "BBA3");
        }
    };

    public CaseDetails executeCcdEventOnCase(CaseDetails caseDetails,
                                             String eventType) {


        Long caseId = caseDetails.getId();
        String caseTypeId = caseDetails.getCaseTypeId();

        log.info("UpdateCase for caseId {} and eventType {}", caseId, eventType);

        StartEventResponse startEventResponse =
            startCaseForCaseworker(systemUserService.getIdamToken(), eventType, caseTypeId);

        CaseDataContent caseDataContent =
            getCaseDataContent(caseDetails.getData(), startEventResponse, eventType);

        return submitEventForCaseworker(systemUserService.getIdamToken(), caseId, caseDataContent, caseTypeId);
    }

    private StartEventResponse startCaseForCaseworker(IdamToken idamToken, String eventId, String caseTypeId) {
        return coreCaseDataApi.startForCaseworker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            "DIVORCE",
            caseTypeId,
            eventId);
    }

    private CaseDetails submitEventForCaseworker(IdamToken idamToken, Long caseId,
                                                 CaseDataContent caseDataContent, String caseTypeId) {
        return coreCaseDataApi.submitEventForCaseWorker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            "DIVORCE",
            caseTypeId,
            caseId.toString(),
            true,
            caseDataContent);
    }

    public CaseDataContent getCaseDataContent(Map caseData,
                                              StartEventResponse startEventResponse,
                                              String summary) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary(summary)
                .description(CCD_EVENT_DESCRIPTION)
                .build())
            .data(caseData)
            .supplementaryDataRequest(supplementaryDataRequestMap)
            .build();
    }
}
