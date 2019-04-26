package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.text.ParseException;


@Service
@Slf4j
@RequiredArgsConstructor
public class CcdUpdateService {
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    public CaseDetails createEvent(String authorisation, CaseDetails caseDetails, String eventId,
                                   String eventSummary, String eventDescription) {


        String caseId = caseDetails.getId().toString();
        String caseType = caseDetails.getCaseTypeId();
        String jurisdictionId = caseDetails.getJurisdiction();

        StartEventResponse startEventResponse = startEvent(authorisation, eventId, caseId, caseType, jurisdictionId);
        CaseDataContent caseDataContent = buildCaseDataContent(caseDetails, eventSummary,
                eventDescription, startEventResponse);
        return submitEvent(authorisation, caseId, caseType, jurisdictionId, caseDataContent);
    }

    private CaseDetails submitEvent(String authorisation, String caseId, String caseType, String jurisdictionId,
                                    CaseDataContent caseDataContent) {
        return coreCaseDataApi.submitEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                getUserId(authorisation),
                jurisdictionId,
                caseType,
                caseId,
                true,
                caseDataContent);
    }

    private StartEventResponse startEvent(String authorisation, String eventId, String caseId, String caseType,
                                          String jurisdictionId) {
        return coreCaseDataApi.startEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                getUserId(authorisation),
                jurisdictionId,
                caseType,
                caseId,
                eventId);
    }

    private CaseDataContent buildCaseDataContent(CaseDetails caseDetails, String eventSummary, String eventDescription,
                                                 StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(buildEvent(eventSummary, eventDescription, startEventResponse))
                .data(caseDetails.getData())
                .build();
    }

    private Event buildEvent(String eventSummary, String eventDescription, StartEventResponse startEventResponse) {
        return Event.builder()
                .id(startEventResponse.getEventId())
                .summary(eventSummary)
                .description(eventDescription)
                .build();
    }

    private String getUserId(String jwt) {
        try {
            jwt = jwt.replaceAll("Bearer ", "");
            return (String) JWTParser.parse(jwt).getJWTClaimsSet().getClaims().get("id");
        } catch (ParseException e) {
            throw new IllegalStateException("JWT is not valid");
        }
    }
}
