package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toListOrNull;

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

    public FinremCaseData mapToFinremCaseData(Map<String, Object> caseData) {
        return mapToFinremCaseData(caseData, null);
    }

    public FinremCaseData mapToFinremCaseData(Map<String, Object> caseData, String caseTypeId) {
        FinremCaseData data = objectMapper.convertValue(caseData, FinremCaseData.class);
        if (caseTypeId != null) {
            data.setCcdCaseType(CaseType.forValue(caseTypeId));
        }
        return data;
    }

    public CaseDetails mapToCaseDetails(FinremCaseDetails caseDetails) {
        Map<String, Object> data = objectMapper.convertValue(caseDetails.getData(), Map.class);
        return CaseDetails.builder().caseTypeId(caseDetails.getCaseType().getCcdType())
            .id(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(caseDetails.getState() != null ? caseDetails.getState().getStateId() : null)
            .createdDate(caseDetails.getCreatedDate())
            .securityLevel(caseDetails.getSecurityLevel())
            .callbackResponseStatus(caseDetails.getCallbackResponseStatus())
            .lastModified(caseDetails.getLastModified())
            .securityClassification(caseDetails.getSecurityClassification())
            .data(data)
            .build();
    }

    /**
     * Converts a {@link FinremCaseDetails} object to a {@link CaseDetails} object,
     * ensuring that fields with {@code null} values are included in the resulting data map.
     *
     * <p>
     * This method creates a copy of the injected {@code ObjectMapper} and applies
     * a MixIn to the specified classes to override any {@code @JsonInclude}
     * annotations that would otherwise exclude null values during serialization.
     * It then serializes the {@code caseDetails} data to JSON (with nulls included),
     * and deserializes it back into a {@link LinkedHashMap}, preserving all keys.
     *
     * <p>
     * The returned {@link CaseDetails} will have its {@code data} field populated
     * with a map that contains all keys, including those with {@code null} values.
     *
     * <p>
     * This is particularly useful when nulls must be sent to the CCD API
     * to explicitly clear fields (e.g. {@code "fieldA": null} to delete {@code fieldA}).
     *
     * <p>
     * @param caseDetails the {@link FinremCaseDetails} instance to convert
     *
     * <p>
     * @param classesToOverrideJsonInclude one or more classes for which {@code null} fields
     * should be forcibly included by applying a MixIn that overrides {@code @JsonInclude}
     *
     * <p>
     * @return a {@link CaseDetails} instance with its data map including null-valued fields
     *
     * <p>
     * @throws JsonProcessingException if an error occurs during JSON serialization or deserialization
     */
    public CaseDetails mapToCaseDetailsIncludingNulls(FinremCaseDetails caseDetails,
                                                      Class... classesToOverrideJsonInclude) throws JsonProcessingException {
        if (toListOrNull(classesToOverrideJsonInclude) == null) {
            return mapToCaseDetails(caseDetails);
        }

        ObjectMapper customMapper = objectMapper.copy();
        // Override class-level @JsonInclude annotations
        for (Class clazz : classesToOverrideJsonInclude) {
            customMapper.addMixIn(clazz, AlwaysIncludeMixIn.class);
        }

        // Now this will include nulls!
        String json = customMapper.writeValueAsString(caseDetails.getData());

        // Deserialize to map with nulls
        LinkedHashMap<String, Object> data = customMapper.readValue(
            json, new TypeReference<>() {}
        );

        return CaseDetails.builder()
            .caseTypeId(caseDetails.getCaseType().getCcdType())
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
