package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentOrder {
    private uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Document orderLetter;
    private Document consentOrder;
    private List<PensionTypeCollection> pensionDocuments;
}
