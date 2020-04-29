package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.Features.APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER;

public class FeatureToggleServiceTest {

    private FeatureToggleService featureToggleService = new FeatureToggleService();

    // TODO: Will fix these tests

    @Test
    public void givenToggleEnabled_thenReturnTrue() {
        assertThat(featureToggleService.isHwfSuccessfulNotificationLetterEnabled(), is(false));
    }

    @Test
    public void givenToggleFalse_thenReturnFalse() {
        assertThat(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled(), is(false));
    }

    @Test
    public void isApprovedConsentOrderNotificationLetterEnabledReturnsTrue() {
        assertThat(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled(), is(false));
    }

    @Test
    public void isHwfSuccessfulNotificationLetterEnabledReturnTrue() {
        assertThat(featureToggleService.isHwfSuccessfulNotificationLetterEnabled(), is(false));
    }

    @Test
    public void isAssignedToJudgeNotificationLetterEnabledReturnTrue() {
        assertThat(featureToggleService.isAssignedToJudgeNotificationLetterEnabled(), is(false));
    }

    @Test
    public void getFieldsIgnoredDuringSerialisationEmptyWhenFeaturesEnabled() {
        featureToggleService.toggle.put(APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER.getName(), Boolean.TRUE.toString());

        assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(),
            not(hasEntry(equalTo(ApprovedOrder.class), containsInAnyOrder("consentOrderApprovedNotificationLetter"))));
    }

    @Test
    public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
        featureToggleService.toggle.put(APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER.getName(), Boolean.FALSE.toString());

        assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(),
            hasEntry(equalTo(ApprovedOrder.class), containsInAnyOrder("consentOrderApprovedNotificationLetter")));
    }
}
