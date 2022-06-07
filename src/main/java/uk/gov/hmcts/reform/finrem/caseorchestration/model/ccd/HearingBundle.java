package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingBundle {

    @JsonProperty("hearingBundleDate")
    private LocalDate hearingBundleDate;
    @JsonProperty("hearingBundleFdr")
    private String hearingBundleFdr;
    @JsonProperty("hearingBundleDocuments")
    private List<HearingUploadBundle> hearingBundleDocuments;
    @JsonProperty("hearingBundleDescription")
    private String hearingBundleDescription;
}
