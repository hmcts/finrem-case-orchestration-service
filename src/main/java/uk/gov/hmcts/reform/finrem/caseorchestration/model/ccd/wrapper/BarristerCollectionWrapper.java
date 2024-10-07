package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BarristerCollectionWrapper {
    @JsonProperty("appBarristerCollection")
    private List<BarristerCollectionItem> applicantBarristers;
    @JsonProperty("respBarristerCollection")
    private List<BarristerCollectionItem> respondentBarristers;
    @JsonProperty("intvr1BarristerCollection")
    private List<BarristerCollectionItem> intvr1Barristers;
    @JsonProperty("intvr2BarristerCollection")
    private List<BarristerCollectionItem> intvr2Barristers;
    @JsonProperty("intvr3BarristerCollection")
    private List<BarristerCollectionItem> intvr3Barristers;
    @JsonProperty("intvr4BarristerCollection")
    private List<BarristerCollectionItem> intvr4Barristers;
}
