package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

public class RemoveNottinghamCourtListGaMigration {

    public static final String NOTTINGHAM_COURT_LIST_GA = "nottinghamCourtListGA";

    public Map<String, Object> migrate(CaseDetails caseDetails) {
        return migrateCaseData(caseDetails.getData());
    }

    public Map<String, Object> migrateCaseData(Map<String, Object> caseData) {
        caseData.remove(NOTTINGHAM_COURT_LIST_GA);
        return caseData;
    }
}
