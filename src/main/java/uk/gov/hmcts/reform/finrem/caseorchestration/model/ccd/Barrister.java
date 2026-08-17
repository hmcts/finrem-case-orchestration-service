package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_BarristerCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Barrister {
    @CCD(label = "Full Name", searchable = false)
    @JsonProperty("name")
    private String name;

    @CCD(label = "Email", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("email")
    private String email;

    @CCD(label = "Phone Number", searchable = false)
    @JsonProperty("phoneNumber")
    private String phone;

    @CCD(label = "Organisation Identifier", searchable = false)
    @JsonProperty("Organisation")
    private Organisation organisation;

    @CCD(label = "User Id", showCondition = "barristerLabel=\"IMPOSSIBLE\"", searchable = false)
    private String userId;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Barrister Details", searchable = false, typeOverride = FieldType.Label)
  private String barristerLabel;
  // ==== end synthesised definition-only fields ====
}
