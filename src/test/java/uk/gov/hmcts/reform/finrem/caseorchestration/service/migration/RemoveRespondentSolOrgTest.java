package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_REF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;


public class RemoveRespondentSolOrgTest extends BaseServiceTest {

    @Autowired
    private RemoveRespondentSolOrg removeRespondentSolOrg;

    @Test
    public void shouldRemoveRespOrgPolicyFromCaseData() {
        Map<String, Object> respPolicy = new HashMap<>();
        respPolicy.put(ORGANISATION_POLICY_ROLE, RESP_SOLICITOR_POLICY);
        respPolicy.put(ORGANISATION_POLICY_REF, null);
        Map<String, Object> org = new HashMap<>();
        org.put(ORGANISATION_POLICY_ORGANISATION_ID, null);
        respPolicy.put(ORGANISATION_POLICY_ORGANISATION, org);

        CaseDetails caseDetails = buildCaseDetails();
        caseDetails.getData().put(ORGANISATION_POLICY_RESPONDENT, respPolicy);

        Map<String, Object> migratedCaseData = removeRespondentSolOrg.migrateCaseData(caseDetails.getData());

        assertNull(migratedCaseData.get(ORGANISATION_POLICY_RESPONDENT));
    }

    @Test
    public void shouldLeaveCaseDataUntouched() {
        CaseDetails caseDetails = buildCaseDetails();
        Map<String, Object> migratedCaseData = removeRespondentSolOrg.migrateCaseData(caseDetails.getData());

        assertTrue(isEmpty(migratedCaseData.get(ORGANISATION_POLICY_RESPONDENT)));
    }
}