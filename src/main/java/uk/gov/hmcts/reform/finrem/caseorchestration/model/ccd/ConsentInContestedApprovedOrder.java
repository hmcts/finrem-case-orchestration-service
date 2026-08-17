package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_roleConsentOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentInContestedApprovedOrder implements HasCaseDocument {
    @CCD(label = "Approved Order Letter", searchable = false, typeOverride = FieldType.Document)
    @JsonProperty("orderLetter")
    private CaseDocument orderLetter;

    @CCD(label = "Consent Order Annexed and Stamped", searchable = false, typeOverride = FieldType.Document)
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

    @CCD(
            label = "Additional Document(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    @JsonProperty("additionalConsentDocuments")
    private List<DocumentCollectionItem> additionalConsentDocuments;

    @CCD(label = "Order received at", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime orderReceivedAt;
}
