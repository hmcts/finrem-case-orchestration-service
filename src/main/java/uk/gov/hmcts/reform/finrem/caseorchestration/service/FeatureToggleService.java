package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.HWF_SUCCESSFUL_NOTIFICATION_LETTER;

@Service
@ConfigurationProperties(prefix = "feature-toggle")
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

    public boolean isApprovedConsentOrderNotificationLetterEnabled() {
        return isFeatureEnabled(APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER);
    }

    public boolean isHwfSuccessfulNotificationLetterEnabled() {
        return isFeatureEnabled(HWF_SUCCESSFUL_NOTIFICATION_LETTER);
    }

    public boolean isAssignedToJudgeNotificationLetterEnabled() {
        return isFeatureEnabled(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER);
    }
}
