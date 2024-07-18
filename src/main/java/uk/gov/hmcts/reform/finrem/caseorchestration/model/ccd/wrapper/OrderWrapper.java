package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderWrapper {
    private List<ApprovedOrderConsolidateCollection> appOrderCollections;
    private List<ApprovedOrderConsolidateCollection> respOrderCollections;
    private List<ApprovedOrderConsolidateCollection> intv1OrderCollections;
    private List<ApprovedOrderConsolidateCollection> intv2OrderCollections;
    private List<ApprovedOrderConsolidateCollection> intv3OrderCollections;
    private List<ApprovedOrderConsolidateCollection> intv4OrderCollections;
    private List<ApprovedOrderCollection> appOrderCollection;
    private List<ApprovedOrderCollection> respOrderCollection;
    private List<ApprovedOrderCollection> intv1OrderCollection;
    private List<ApprovedOrderCollection> intv2OrderCollection;
    private List<ApprovedOrderCollection> intv3OrderCollection;
    private List<ApprovedOrderCollection> intv4OrderCollection;
}
