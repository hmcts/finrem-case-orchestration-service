package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonProperty("email")
    private String email;

    @JsonProperty("openingHours")
    private String openingHours;

    @JsonIgnore
    public String getCourtContactDetailsAsOneLineAddressString() {
        return StringUtils.joinWith(", ", courtName, courtAddress);
    }
}
