package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;

import javax.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.ASSIGN_CASE_ACCESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.CASEWORKER_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.INTERVENER_ENABLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.PAYMENT_REQUEST_USING_CASE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.SECURE_DOC_ENABLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.SEND_LETTER_RECIPIENT_CHECK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.SOLICITOR_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.USE_USER_TOKEN;

/**
 * To add a feature toggle flag:
 * <ul>
 *     <li>add an entry in application properties with key prefixed with {@code feature.toggle.}, eg.
 *     {@code feature.toggle.blah} that should have value of {@code true} or {@code false}</li>
 *     <li>add an entry to {@link Features} class, eg. {@code BLAH("blah")}</li>
 *     <li>add appropriate method to check if feature is enabled, eg. {@code public boolean isBlahEnabled()}</li>
 * </ul>
 * Spring configuration will populate {@link #toggle} map with values from properties file.
 */
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

    /*
     * Defaulted to true. Only to be set to false in Preview as ACA API is not deployed there
     */
    public boolean isAssignCaseAccessEnabled() {
        return isFeatureEnabled(ASSIGN_CASE_ACCESS);
    }

    public boolean isPBAUsingCaseTypeEnabled() {
        return isFeatureEnabled(PAYMENT_REQUEST_USING_CASE_TYPE);
    }

    public boolean isUseUserTokenEnabled() {
        return isFeatureEnabled(USE_USER_TOKEN);
    }

    public boolean isSolicitorNoticeOfChangeEnabled() {
        return isFeatureEnabled(SOLICITOR_NOTICE_OF_CHANGE);
    }

    /*
     * Used for sending emails to FRC in Notification Service
     * Removing will result in test account being emailed, rather than actual FRCs
     * Court Emails are defined in court-details.json
     */
    public boolean isCaseworkerNoCEnabled() {
        return isFeatureEnabled(CASEWORKER_NOTICE_OF_CHANGE);
    }

    public boolean isSendLetterDuplicateCheckEnabled() {
        return isFeatureEnabled(SEND_LETTER_RECIPIENT_CHECK);
    }

    public boolean isSecureDocEnabled() {
        return isFeatureEnabled(SECURE_DOC_ENABLED);
    }

    public boolean isIntervenerEnabled() {
        return isFeatureEnabled(INTERVENER_ENABLED);
    }

    /**
     * Given runtime feature toggle status, returns fields that should be ignored during serialisation (i.e. not
     * serialised to JSON).
     *
     * @return a map with Class of ignored fields as key and field names as value
     */
    @SuppressWarnings("java:S3740")
    public Map<Class, List<String>> getFieldsIgnoredDuringSerialisation() {
        Map<Class, List<String>> ignoredFields = Maps.newHashMap();


        ignoredFields.put(UploadCaseDocument.class, Arrays.asList("caseDocumentConfidential", "hearingDetails"));


        return ignoredFields;
    }
}
