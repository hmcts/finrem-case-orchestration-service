package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FinremCaseDetails<T extends FinremCaseData> implements CcdCaseDetails<T> {

    private Long id;
    private String jurisdiction;
    private State state;
    private LocalDateTime createdDate;
    private Integer securityLevel;
    private String callbackResponseStatus;
    private LocalDateTime lastModified;
    private Classification securityClassification;
    @JsonProperty("case_data")
    @JsonAlias("data")
    private T data;

    @JsonProperty("case_type_id")
    private CaseType caseType;

    @JsonProperty("locked_by_user_id")
    private Integer lockedBy;
}
