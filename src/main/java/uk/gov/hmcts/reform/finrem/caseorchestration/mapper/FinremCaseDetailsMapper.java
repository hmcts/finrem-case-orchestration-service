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

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinremCaseDetailsMapper {

    private final ObjectMapper objectMapper;

    public FinremCaseDetails mapToFinremCaseDetails(CaseDetails caseDetails) {
        FinremCaseData data = objectMapper.convertValue(caseDetails.getData(), FinremCaseData.class);
        data.setCcdCaseType(CaseType.forValue(caseDetails.getCaseTypeId()));
        return FinremCaseDetails.builder()
            .caseType(CaseType.forValue(caseDetails.getCaseTypeId()))
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
        Map data = objectMapper.convertValue(caseDetails.getData(), Map.class);
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
