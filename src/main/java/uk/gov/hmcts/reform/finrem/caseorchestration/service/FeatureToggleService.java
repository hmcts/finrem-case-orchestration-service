package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.HWF_SUCCESSFUL_NOTIFICATION_LETTER;

@Service
@ConfigurationProperties(prefix = "feature")
@Configuration
@Getter
public class FeatureToggleService {

    public static final Map<Class, List<String>> APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER_FEATURE_FIELDS
        = ImmutableMap.of(ApprovedOrder.class, asList("consentOrderApprovedNotificationLetter"));

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

    /**
     * Given runtime feature toggle status, returns fields that should be ignored during serialisation (i.e. not
     * serialised to JSON).
     * All CaseFields in "-nonprod.json" files in CCD config repos should be added here to feature toggle serialisation.
     * @return a map with Class of ignored fields as key and field names as value
     */
    public Map<Class, List<String>> getFieldsIgnoredDuringSerialisation() {
        Map<Class, List<String>> ignoredFields = Maps.newHashMap();

        if (!isApprovedConsentOrderNotificationLetterEnabled()) {
            ignoredFields.putAll(APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER_FEATURE_FIELDS);
        }

        return ignoredFields;
    }
}
