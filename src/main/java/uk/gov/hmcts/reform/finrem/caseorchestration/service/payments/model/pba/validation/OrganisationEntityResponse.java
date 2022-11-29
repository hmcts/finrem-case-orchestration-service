package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationEntityResponse {

    @JsonProperty("organisationIdentifier")
    private String organisationIdentifier;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "sraId")
    private String sraId;

    @JsonProperty(value = "sraRegulated")
    private boolean sraRegulated;

    @JsonProperty(value = "companyNumber")
    private String companyNumber;

    @JsonProperty(value = "companyUrl")
    private String companyUrl;

    @JsonProperty(value = "paymentAccount")
    private List<String> paymentAccount;

    @JsonProperty(value = "superUser")
    private SuperUserResponse superUser;

    @JsonProperty(value = "contactInformation")
    private List<ContactInformationResponse> contactInformation;
}
