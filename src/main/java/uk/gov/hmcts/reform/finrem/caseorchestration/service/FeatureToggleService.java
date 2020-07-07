package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.CONSENT_ORDER_NOT_APPROVED_APPLICANT_DOCUMENT_GENERATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.CONTESTED_COURT_DETAILS_MIGRATION;

@Service
@ConfigurationProperties(prefix = "feature")
@Configuration
@Getter
public class FeatureToggleService {

    @NotNull
    private Map<String, String> toggle = new HashMap<>();

    private boolean isFeatureEnabled(Features feature) {
        return Optional.ofNullable(toggle.get(feature.getName()))
            .map(Boolean::parseBoolean)
            .orElse(false);
    }

    public boolean isConsentOrderNotApprovedApplicantDocumentGenerationEnabled() {
        return isFeatureEnabled(CONSENT_ORDER_NOT_APPROVED_APPLICANT_DOCUMENT_GENERATION);
    }

    public boolean isContestedCourtDetailsMigrationEnabled() {
        return isFeatureEnabled(CONTESTED_COURT_DETAILS_MIGRATION);
    }

    /**
     * Given runtime feature toggle status, returns fields that should be ignored during serialisation (i.e. not
     * serialised to JSON).
     * All CaseFields in "-nonprod.json" files in CCD config repos should be added here to feature toggle serialisation.
     * @return a map with Class of ignored fields as key and field names as value
     */
    public Map<Class, List<String>> getFieldsIgnoredDuringSerialisation() {
        Map<Class, List<String>> ignoredFields = Maps.newHashMap();

        return ignoredFields;
    }
}
