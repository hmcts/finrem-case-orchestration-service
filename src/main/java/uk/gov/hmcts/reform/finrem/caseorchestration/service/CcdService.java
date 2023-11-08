package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdService {

    private static final String JURISDICTION = "DIVORCE";
    private final CaseEventsApi caseEventsApi;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamAuthService idamAuthService;
    private static final String LOGGER =  "Executing eventType {} on caseId {}";

    public void executeCcdEventOnCase(String authorisation, String caseId, String caseTypeId,
                                      String eventType) {

        log.info(LOGGER, eventType, caseId);

        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi
            .startEventForCaseWorker(idamToken.getIdamOauth2Token(),
                idamToken.getServiceAuthorization(),
                idamToken.getUserId(),
                JURISDICTION,
                caseTypeId,
                caseId,
                eventType);

        coreCaseDataApi.submitEventForCaseWorker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            JURISDICTION,
            caseTypeId,
            caseId,
            true,
            getCaseDataContent(startEventResponse.getCaseDetails().getData(), startEventResponse));
    }

    @Retryable
    public void executeCcdEventOnCase(CaseDetails caseDetails, String authorisation, String caseId, String caseTypeId,
                                      String eventType, String summary, String description) {

        log.info(LOGGER, eventType, caseId);

        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi
            .startEventForCaseWorker(idamToken.getIdamOauth2Token(),
                idamToken.getServiceAuthorization(),
                idamToken.getUserId(),
                JURISDICTION,
                caseTypeId,
                caseId,
                eventType);

        coreCaseDataApi.submitEventForCaseWorker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            JURISDICTION,
            caseTypeId,
            caseId,
            true,
            getCaseDataContent(caseDetails.getData(), summary, description, startEventResponse));
    }

    private CaseDataContent getCaseDataContent(Object caseData,
                                               StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(caseData)
            .build();
    }

    private CaseDataContent getCaseDataContent(Object caseData, String summary, String description,
                                               StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary(summary)
                .description(description)
                .build())
            .data(caseData)
            .build();
    }

    public List<CaseEventDetail> getCcdEventDetailsOnCase(String authorisation, CaseDetails caseDetails,
                                      String eventType) {
        Long caseId = caseDetails.getId();
        String caseTypeId = caseDetails.getCaseTypeId();

        log.info(LOGGER, eventType, caseId);

        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);

        return caseEventsApi.findEventDetailsForCase(idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            JURISDICTION,
            caseTypeId,
            caseId.toString());
    }

    public SearchResult getCaseByCaseId(String caseId, CaseType caseType, String authorisation) {
        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);
        SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
        String escapeValue = StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJson(caseId));
        searchBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("reference", escapeValue).operator(Operator.AND)));

        return coreCaseDataApi.searchCases(idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(), caseType.getCcdType(), searchBuilder.toString());
    }
}
