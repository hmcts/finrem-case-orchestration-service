package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CaseFlagsConfiguration {
    private final String hmctsId;

    public CaseFlagsConfiguration(@Value("${case-flags.supplementary-data.hmctsid}") String hmctsId) {
        this.hmctsId = hmctsId;
    }
}