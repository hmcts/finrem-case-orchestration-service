package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FrcCourtDetails {
    @JsonProperty("courtName")
    private String courtName;

    @JsonProperty("courtAddress")
    private String courtAddress;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("frcName")
    private String frcName;

    @JsonProperty("email")
    private String email;
}
