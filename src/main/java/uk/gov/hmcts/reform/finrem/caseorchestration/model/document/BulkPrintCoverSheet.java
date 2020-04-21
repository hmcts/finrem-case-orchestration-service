package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class BulkPrintCoverSheet {

    @JsonProperty("RecipientName")
    private String recipientName;

    @JsonProperty("County")
    private String county;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("PostCode")
    private String postCode;

    @JsonProperty("PostTown")
    private String postTown;

    @JsonProperty("AddressLine1")
    private String addressLine1;

    @JsonProperty("AddressLine2")
    private String addressLine2;

    @JsonProperty("AddressLine3")
    private String addressLine3;

    @JsonProperty("CCDNumber")
    private String ccdNumber;
}
