package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentOrderCollection implements CaseDocumentsDiscovery {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private ApprovedOrder approvedOrder;

    @Override
    public List<CaseDocument> discover() {
        List<CaseDocument> consentOrderDocuments = ofNullable(approvedOrder)
            .map(ApprovedOrder::getConsentOrder)
            .map(List::of)
            .orElse(List.of());

        List<CaseDocument> orderLetterDocuments = ofNullable(approvedOrder)
            .map(ApprovedOrder::getOrderLetter)
            .map(List::of)
            .orElse(List.of());

        List<CaseDocument> pensionDocuments = ofNullable(approvedOrder)
            .map(ApprovedOrder::getPensionDocuments)
            .orElse(List.of())
            .stream()
            .flatMap(pensionTypeCollection -> pensionTypeCollection.discover().stream())
            .toList();

        return Stream.concat(
                Stream.concat(consentOrderDocuments.stream(), orderLetterDocuments.stream()),
                pensionDocuments.stream())
            .toList();
    }
}
