package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;

public class CaseDataServiceTest extends BaseServiceTest {
    @Autowired
    CaseDataService caseDataService;

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_shouldReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, RESPONDENT_SOLICITOR);
        assertTrue(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_appSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, APPLICANT_SOLICITOR);
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_fieldNotExist() {
        Map<String, Object> caseData = new HashMap<>();
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }
}