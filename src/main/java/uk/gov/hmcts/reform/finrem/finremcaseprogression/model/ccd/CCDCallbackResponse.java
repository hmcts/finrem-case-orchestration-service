package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDCallbackResponse {

    private CaseData data;
    private List<String> errors;
    private List<String> warnings;
}

