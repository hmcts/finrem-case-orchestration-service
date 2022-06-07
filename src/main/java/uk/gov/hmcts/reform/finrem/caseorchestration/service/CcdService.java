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

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;

    public CaseDetails executeCcdEventOnCase(String authorisation, CaseDetails caseDetails,
                                             String eventType) {


        Long caseId = caseDetails.getId();
        String caseTypeId = caseDetails.getCaseTypeId();

        log.info("Executing eventType {} on caseId {}", eventType, caseId);

        IdamToken idamToken = systemUserService.getIdamToken(authorisation);

        StartEventResponse startEventResponse =
            startCaseForCaseworker(idamToken, eventType, caseTypeId, caseDetails.getId());

        return submitEventForCaseworker(
            idamToken,
            caseId,
            getCaseDataContent(caseDetails.getData(), startEventResponse),
            caseTypeId);
    }

    private StartEventResponse startCaseForCaseworker(IdamToken idamToken, String eventId, String caseTypeId, Long caseId) {
        return coreCaseDataApi.startEventForCaseWorker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            "DIVORCE",
            caseTypeId,
            caseId.toString(),
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

    private CaseDataContent getCaseDataContent(Object caseData,
                                               StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(caseData)
            .supplementaryDataRequest(
                Collections.singletonMap("$set", Collections.singletonMap("HMCTSServiceId", "BBA3")))
            .build();
    }
}
