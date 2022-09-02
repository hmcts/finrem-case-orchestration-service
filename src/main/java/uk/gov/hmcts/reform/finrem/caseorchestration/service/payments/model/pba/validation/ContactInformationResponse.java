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
public class ContactInformationResponse {

    @JsonProperty(value = "addressLine1")
    private String addressLine1;
    @JsonProperty(value = "addressLine2")
    private String addressLine2;
    @JsonProperty(value = "addressLine3")
    private String addressLine3;
    @JsonProperty(value = "townCity")
    private String townCity;
    @JsonProperty(value = "county")
    private String county;
    @JsonProperty(value = "country")
    private String country;
    @JsonProperty(value = "postCode")
    private String postCode;
    @JsonProperty(value = "dxAddress")
    private List<DxAddressResponse> dxAddress;
}
