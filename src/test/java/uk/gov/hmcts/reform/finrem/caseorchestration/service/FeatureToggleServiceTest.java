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

    private FeatureToggleService classToTest = new FeatureToggleService();

    @Test
    public void isApprovedConsentOrderNotificationLetterEnabledReturnsTrue() {
        assertThat(classToTest.isApprovedConsentOrderNotificationLetterEnabled(), is(false));
    }

    @Test
    public void isHwfSuccessfulNotificationLetterEnabledReturnTrue() {
        assertThat(classToTest.isHwfSuccessfulNotificationLetterEnabled(), is(false));
    }

    @Test
    public void isAssignedToJudgeNotificationLetterEnabledReturnTrue() {
        assertThat(classToTest.isAssignedToJudgeNotificationLetterEnabled(), is(false));
    }

    @Test
    public void getFieldsIgnoredDuringSerialisationEmptyWhenFeaturesEnabled() {
        classToTest.toggle.put(APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER.getName(), Boolean.TRUE.toString());

        assertThat(classToTest.getFieldsIgnoredDuringSerialisation(),
            not(hasEntry(equalTo(ApprovedOrder.class), containsInAnyOrder("consentOrderApprovedNotificationLetter"))));
    }

    @Test
    public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
        classToTest.toggle.put(APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER.getName(), Boolean.FALSE.toString());

        assertThat(classToTest.getFieldsIgnoredDuringSerialisation(),
            hasEntry(equalTo(ApprovedOrder.class), containsInAnyOrder("consentOrderApprovedNotificationLetter")));
    }
}
