package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_consentOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovedOrder implements HasCaseDocument {
    @CCD(label = "Approved Order Letter", searchable = false, typeOverride = FieldType.Document)
    @JsonProperty("orderLetter")
    private CaseDocument orderLetter;
    @CCD(
            label = "Consent Order Annexed and Stamped",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("consentOrder")
    private CaseDocument consentOrder;
    @CCD(
            label = "Pension Documents Stamped",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_PensionType"
    )
    @JsonProperty("pensionDocuments")
    private List<PensionTypeCollection> pensionDocuments;
}
