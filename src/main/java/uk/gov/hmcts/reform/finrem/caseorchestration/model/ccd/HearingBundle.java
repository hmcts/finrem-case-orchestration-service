package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingBundle {

    @JsonProperty("hearingBundleDate")
    private LocalDate hearingBundleDate;
    @JsonProperty("bundleDocuments")
    private CaseDocument bundleDocuments;
    @JsonProperty("bundleUploadDate")
    private LocalDate bundleUploadDate;
    @JsonProperty("hearingBundleDescription")
    private String hearingBundleDescription;
}
