package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremFormCandGCorresponder;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingDocumentService {
    protected static final String HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE = "This listing notice must be sent to the applicant and respondent"
        + " as default. If this listing needs to be sent to only one of these parties please use the general order event.";
    private final FinremFormCandGCorresponder finremFormCandGCorresponder;

    @SuppressWarnings("java:S1874")
    public void addCourtFields(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        data.put("courtDetails", buildFrcCourtDetails(data));
    }

    public void sendInitialHearingCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        finremFormCandGCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }
}
