package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public void moveCollection(Map<String, Object> caseData, String sourceFieldName, String destinationFieldName) {
        if (caseData.get(sourceFieldName) != null && (caseData.get(sourceFieldName) instanceof Collection)) {
            if (caseData.get(destinationFieldName) == null || (caseData.get(destinationFieldName) instanceof Collection)) {
                final List destinationList = new ArrayList();
                if (caseData.get(destinationFieldName) != null) {
                    destinationList.addAll((List) caseData.get(destinationFieldName));
                }
                destinationList.addAll((List) caseData.get(sourceFieldName));
                caseData.put(destinationFieldName, destinationList);
                caseData.put(sourceFieldName, null);
            }
        }
    }
}
