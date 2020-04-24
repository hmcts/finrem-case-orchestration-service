package uk.gov.hmcts.reform.finrem.caseorchestration.service.impl;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@ConfigurationProperties(prefix = "feature-toggle")
@Configuration
@Getter
public class FeatureToggleServiceImpl implements FeatureToggleService {

    @NotNull
    private Map<String, String> toggle = new HashMap<>();

    @Override
    public boolean isFeatureEnabled(Features feature) {
        return Optional.ofNullable(toggle.get(feature.getName()))
            .map(Boolean::parseBoolean)
            .orElse(false);
    }
}
