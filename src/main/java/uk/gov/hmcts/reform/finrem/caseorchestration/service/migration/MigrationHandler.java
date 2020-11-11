package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

public interface MigrationHandler {
    Map<String, Object> migrate(CaseDetails caseDetails);
}
