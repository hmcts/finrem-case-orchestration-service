package uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.request.RequestData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.elasticsearch.MatchQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {

    public static final String UPDATE_CASE_EVENT = "internal-change-UPDATE_CASE";

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final RequestData requestData;
    private final SystemUserService systemUserService;
    private final CCDConcurrencyHelper concurrencyHelper;
    private final ConcurrentHashMap<Long, AtomicInteger> concurrentMap = new ConcurrentHashMap<>();

    // Required so calls to the same class get proxied correctly and have the retry annotation applied
    @Lazy
    @Resource(name = "coreCaseDataService")
    private CoreCaseDataService self;

    public CaseDetails performPostSubmitCallbackWithoutChange(CaseType caseType, Long caseId, String eventName) {
        return self.performPostSubmitCallback(caseType, caseId, eventName, (caseDetails) -> Map.of(), true);
    }

    public CaseDetails performPostSubmitCallback(CaseType caseType,
                                                 Long caseId,
                                                 String eventName,
                                                 Function<CaseDetails, Map<String, Object>> changeFunction) {
        return self.performPostSubmitCallback(caseType, caseId, eventName, changeFunction, false);
    }

    @Retryable(recover = "recover", maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public CaseDetails performPostSubmitCallback(CaseType caseType,
                                                 Long caseId,
                                                 String eventName,
                                                 Function<CaseDetails, Map<String, Object>> changeFunction,
                                                 boolean submitIfEmpty) {
        AtomicInteger lock = concurrentMap.computeIfAbsent(caseId, (key) -> new AtomicInteger(0));
        lock.addAndGet(1);

        try {
            synchronized (lock) {
                StartEventResponse startEventResponse = concurrencyHelper.startEvent(caseType, caseId, eventName);
                CaseDetails caseDetails = startEventResponse.getCaseDetails();
                // Work around immutable maps
                HashMap<String, Object> caseDetailsMap = new HashMap<>(caseDetails.getData());
                caseDetails.setData(caseDetailsMap);

                Map<String, Object> updates = changeFunction.apply(caseDetails);

                if (!updates.isEmpty() || submitIfEmpty) {
                    log.info("Submitting event {} on case {}", eventName, caseId);
                    concurrencyHelper.submitEvent(startEventResponse, caseType, caseId, updates);
                } else {
                    log.info("No updates, skipping submit event");
                }
                caseDetails.getData().putAll(updates);
                return caseDetails;
            }
        } finally {
            concurrentMap.computeIfPresent(caseId, (key, value) -> value.addAndGet(-1) <= 0 ? null : value);
        }
    }

    @Recover
    void recover(Exception e, Long caseId, String eventName,
                 Function<CaseDetails, Map<String, Object>> changeFunction,
                 boolean submitIfEmpty) {
        throw new RetryFailureException(
            String.format("All retries failed to create event %s on ccd for case %d", eventName, caseId), e);
    }

    public CaseDetails findCaseDetailsById(final String caseId) {
        return coreCaseDataApi.getCase(requestData.authorisation(), authTokenGenerator.generate(), caseId);
    }

    public CaseDetails findCaseDetailsByIdNonUser(CaseType caseType, String caseId) {
        String userToken = systemUserService.getSysUserToken();

        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), caseType.getCcdType(),
            new MatchQuery("reference", caseId).toQueryContext(1, 0).toString()).getCases().getFirst();
    }

    public SearchResult searchCases(CaseType caseType, String query) {
        String userToken = systemUserService.getSysUserToken();

        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), caseType.getCcdType(), query);
    }
}
