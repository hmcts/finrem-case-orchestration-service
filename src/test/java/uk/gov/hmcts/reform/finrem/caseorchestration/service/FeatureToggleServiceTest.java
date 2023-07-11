package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Enclosed.class)
public class FeatureToggleServiceTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.send_to_frc=true",
        "feature.toggle.assign_case_access=true",
        "feature.toggle.pba_case_type=true"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOn extends BaseServiceTest {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isSendToFRCEnabledReturnsTrue() {
            assertThat(featureToggleService.isSendToFRCEnabled(), is(true));
        }

        @Test
        public void isAssignCaseAccessEnabledReturnsTrue() {
            assertThat(featureToggleService.isAssignCaseAccessEnabled(), is(true));
        }

        @Test
        public void isPbaToggleEnabledReturnsTrue() {
            assertThat(featureToggleService.isPBAUsingCaseTypeEnabled(), is(true));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.send_to_frc=false",
        "feature.toggle.assign_case_access=false",
        "feature.toggle.pba_case_type=false",
        "feature.toggle.send_letter_recipient_check=false",
        "feature.toggle.intervener_enabled=false"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOff extends BaseServiceTest {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            Map<Class, List<String>> ignoredFields = Maps.newHashMap();
            ignoredFields.put(ContestedUploadedDocument.class, Arrays.asList("caseDocumentConfidential", "hearingDetails"));
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(ignoredFields));
        }

        @Test
        public void isSendToFRCEnabledReturnsFalse() {
            assertThat(featureToggleService.isSendToFRCEnabled(), is(false));
        }

        @Test
        public void isAssignCaseAccessEnabledReturnsFalse() {
            assertThat(featureToggleService.isAssignCaseAccessEnabled(), is(false));
        }

        @Test
        public void isPbaToggleEnabledReturnsFalse() {
            assertThat(featureToggleService.isPBAUsingCaseTypeEnabled(), is(false));
        }

        @Test
        public void isSendLetterDuplicateCheckReturnsFalse() {
            assertThat(featureToggleService.isSendLetterDuplicateCheckEnabled(), is(false));
        }

        @Test
        public void isIntervenerEnabled() {
            assertThat(featureToggleService.isIntervenerEnabled(), is(false));
        }

    }
}
