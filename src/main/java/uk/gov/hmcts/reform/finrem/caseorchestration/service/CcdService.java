package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

    public void executeCcdEventOnCase(String authorisation, CaseDetails caseDetails,
                                      String eventType) {

        Long caseId = caseDetails.getId();
        String caseTypeId = caseDetails.getCaseTypeId();

        log.info("Executing eventType {} on caseId {}", eventType, caseId);

        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi
            .startEventForCaseWorker(idamToken.getIdamOauth2Token(),
                idamToken.getServiceAuthorization(),
                idamToken.getUserId(),
                JURISDICTION,
                caseTypeId,
                caseId.toString(),
                eventType);

        coreCaseDataApi.submitEventForCaseWorker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            JURISDICTION,
            caseTypeId,
            caseId.toString(),
            true,
            getCaseDataContent(startEventResponse.getCaseDetails().getData(), startEventResponse));
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

    public List<CaseEventDetail> getCcdEventDetailsOnCase(String authorisation, CaseDetails caseDetails,
                                      String eventType) {
        Long caseId = caseDetails.getId();
        String caseTypeId = caseDetails.getCaseTypeId();

        log.info("Executing eventType {} on caseId {}", eventType, caseId);

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
        searchBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("data.caseReference", escapeValue).operator(Operator.AND)));
        log.info("Elasticsearch query {}", searchBuilder);
        return coreCaseDataApi.searchCases(idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(), caseType.getCcdType(), searchBuilder.toString());
    }
}
