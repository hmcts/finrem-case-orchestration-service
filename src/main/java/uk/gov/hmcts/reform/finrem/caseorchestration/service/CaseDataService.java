package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseDataService {

    public boolean isRespondentSolicitorResponsibleToDraftOrder(Map<String, Object> caseData) {
        return RESPONDENT_SOLICITOR.equals(nullToEmpty(caseData.get(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER)));
    }
}
