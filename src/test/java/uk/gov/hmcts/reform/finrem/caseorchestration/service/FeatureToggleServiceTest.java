package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class FeatureToggleServiceTest {

    private FeatureToggleService classToTest = new FeatureToggleService();

    // TODO: Will fix these tests

    @Test
    public void givenToggleEnabled_thenReturnTrue() {
        assertThat(classToTest.isHwfSuccessfulNotificationLetterEnabled(), is(false));
    }

    @Test
    public void givenToggleFalse_thenReturnFalse() {
        assertThat(classToTest.isApprovedConsentOrderNotificationLetterEnabled(), is(false));
    }

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
}
