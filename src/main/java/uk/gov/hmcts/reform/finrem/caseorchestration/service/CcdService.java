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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
    private static final String LOGGER = "Executing eventType {} on Case ID: {}";

    public void executeCcdEventOnCase(String authorisation, String caseId, String caseTypeId,
                                      String eventType) {

        StartEventResponse startEventResponse = startEventForCaseWorker(authorisation, caseId, caseTypeId, eventType);

        submitEventForCaseWorker(startEventResponse, authorisation, caseId, caseTypeId, eventType, "", "");
    }

    /**
     * Start a CCD event.
     * <p>The event should be submitted by a subsequent call to {@link #submitEventForCaseWorker}.</p>
     * @param authorisation auth token
     * @param caseId case id
     * @param caseTypeId case type id
     * @param eventType case event to start
     * @return StartEventResponse
     */
    public StartEventResponse startEventForCaseWorker(String authorisation, String caseId, String caseTypeId,
                                                      String eventType) {
        log.info(LOGGER, eventType, caseId);

        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);

        return coreCaseDataApi
            .startEventForCaseWorker(idamToken.getIdamOauth2Token(),
                idamToken.getServiceAuthorization(),
                idamToken.getUserId(),
                JURISDICTION,
                caseTypeId,
                caseId,
                eventType);
    }

    /**
     * Submit an event to CCD.
     * <p>The case data in {@code startEventResponse} should be from the return value of the initial call to
     * {@link #startEventForCaseWorker}. Do not use case data from another source to avoid data loss due to concurrent
     * case data updates.</p>
     * @param startEventResponse case data
     * @param authorisation auth token
     * @param caseId case id
     * @param caseTypeId case type id
     * @param eventType case event to submit
     * @param summary event summary
     * @param description event description
     */
    public void submitEventForCaseWorker(StartEventResponse startEventResponse, String authorisation, String caseId, String caseTypeId,
                                         String eventType, String summary, String description) {
        log.info(LOGGER, eventType, caseId);

        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);

        coreCaseDataApi.submitEventForCaseWorker(
            idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(),
            idamToken.getUserId(),
            JURISDICTION,
            caseTypeId,
            caseId,
            true,
            getCaseDataContent(startEventResponse.getCaseDetails().getData(), summary, description, startEventResponse));
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

    public List<CaseEventDetail> getCcdEventDetailsOnCase(String authorisation, FinremCaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        String caseTypeId = caseDetails.getCaseType().getCcdType();
        log.info(LOGGER, caseTypeId, caseId);

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

    public SearchResult esSearchCases(CaseType caseType, String esQueryString, String authorisation) {
        IdamToken idamToken = idamAuthService.getIdamToken(authorisation);
        return coreCaseDataApi.searchCases(idamToken.getIdamOauth2Token(),
            idamToken.getServiceAuthorization(), caseType.getCcdType(), esQueryString);
    }

}
