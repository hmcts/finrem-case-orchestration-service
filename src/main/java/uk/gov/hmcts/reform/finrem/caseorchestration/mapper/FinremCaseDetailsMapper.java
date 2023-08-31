package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinremCaseDetailsMapper {

    private final ObjectMapper objectMapper;

    public FinremCaseDetails mapToFinremCaseDetails(CaseDetails caseDetails) {
        String caseTypeId = caseDetails.getCaseTypeId();
        Map<String, Object> caseDataCopy = new HashMap<>(caseDetails.getData());
        caseDataCopy.put("ccdCaseType", caseTypeId);
        FinremCaseData data = objectMapper.convertValue(caseDataCopy, FinremCaseData.class);
        return FinremCaseDetails.builder()
            .caseType(CaseType.forValue(caseTypeId))
            .id(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(caseDetails.getState() != null ? State.forValue(caseDetails.getState()) : null)
            .createdDate(caseDetails.getCreatedDate())
            .securityLevel(caseDetails.getSecurityLevel())
            .callbackResponseStatus(caseDetails.getCallbackResponseStatus())
            .lastModified(caseDetails.getLastModified())
            .securityClassification(caseDetails.getSecurityClassification())
            .data(data)
            .build();
    }

    public CaseDetails mapToCaseDetails(FinremCaseDetails caseDetails) {
        Map<String, Object> data = objectMapper.convertValue(caseDetails.getData(), Map.class);
        return CaseDetails.builder().caseTypeId(caseDetails.getCaseType().getCcdType())
            .id(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(caseDetails.getState().getStateId())
            .createdDate(caseDetails.getCreatedDate())
            .securityLevel(caseDetails.getSecurityLevel())
            .callbackResponseStatus(caseDetails.getCallbackResponseStatus())
            .lastModified(caseDetails.getLastModified())
            .securityClassification(caseDetails.getSecurityClassification())
            .data(data)
            .build();
    }

}
