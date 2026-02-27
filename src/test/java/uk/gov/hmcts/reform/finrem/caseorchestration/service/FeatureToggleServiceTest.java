package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FeatureToggleServiceTest {

    @Nested
    class DefaultToggleValuesTests {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        void isAssignCaseAccessEnabledReturnsTrue() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isAssignCaseAccessEnabled()).isTrue();
        }

        @Test
        void isCaseFileViewEnabledReturnsFalse() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isCaseFileViewEnabled()).isFalse();
        }

        @Test
        void isExpressPilotEnabledReturnsTrue() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isExpressPilotEnabled()).isFalse();
        }

        @Test
        void isExui3990WorkaroundEnabledReturnsTrue() {
            // not in any application.properties
            assertThat(featureToggleService.isExui3990WorkaroundEnabled()).isTrue();
        }

        @Test
        void isIntervenerEnabledReturnsFalse() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isIntervenerEnabled()).isFalse();
        }

        @Test
        void isPBAUsingCaseTypeEnabledReturnsFalse() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isPBAUsingCaseTypeEnabled()).isFalse();
        }

        @Test
        void isSecureDocEnabledReturnsTrue() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isSecureDocEnabled()).isTrue();
        }

        @Test
        void isSendLetterDuplicateCheckEnabledReturnsTrue() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isSendLetterDuplicateCheckEnabled()).isTrue();
        }

        @Test
        void isSendToFRCEnabledReturnsFalse() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isSendToFRCEnabled()).isFalse();
        }

        @Test
        void isVacateHearingEnabledReturnsFalse() {
            // default value is defined in src/test/resources/application.properties
            assertThat(featureToggleService.isVacateHearingEnabled()).isFalse();
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "feature.toggle.send_to_frc=true",
        "feature.toggle.assign_case_access=true",
        "feature.toggle.pba_case_type=true"
    })
    class ApprovedConsentOrderNotificationSwitchedOn {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        void isSendToFRCEnabledReturnsTrue() {
            assertThat(featureToggleService.isSendToFRCEnabled()).isTrue();
        }

        @Test
        void isAssignCaseAccessEnabledReturnsTrue() {
            assertThat(featureToggleService.isAssignCaseAccessEnabled()).isTrue();
        }

        @Test
        void isPbaToggleEnabledReturnsTrue() {
            assertThat(featureToggleService.isPBAUsingCaseTypeEnabled()).isTrue();
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "feature.toggle.send_to_frc=false",
        "feature.toggle.assign_case_access=false",
        "feature.toggle.pba_case_type=false",
        "feature.toggle.send_letter_recipient_check=false",
        "feature.toggle.secure_doc_enabled=false",
        "feature.toggle.intervener_enabled=false",
        "feature.toggle.case_file_view_enabled=false",
        "feature.toggle.express_pilot_enabled=false",
        "feature.toggle.manage_hearing_enabled=false",
        "feature.toggle.vacate_hearing_enabled=false"
    })
    class ApprovedConsentOrderNotificationSwitchedOff {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            Map<Class, List<String>> ignoredFields = new HashMap<>();
            ignoredFields.put(
                UploadCaseDocument.class,
                List.of("caseDocumentConfidential", "hearingDetails")
            );

            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation())
                .isEqualTo(ignoredFields);
        }

        @Test
        void isSendToFRCEnabledReturnsFalse() {
            assertThat(featureToggleService.isSendToFRCEnabled()).isFalse();
        }

        @Test
        void isAssignCaseAccessEnabledReturnsFalse() {
            assertThat(featureToggleService.isAssignCaseAccessEnabled()).isFalse();
        }

        @Test
        void isPbaToggleEnabledReturnsFalse() {
            assertThat(featureToggleService.isPBAUsingCaseTypeEnabled()).isFalse();
        }

        @Test
        void isSendLetterDuplicateCheckReturnsFalse() {
            assertThat(featureToggleService.isSendLetterDuplicateCheckEnabled()).isFalse();
        }

        @Test
        void isSecureDocEnabledReturnsFalse() {
            assertThat(featureToggleService.isSecureDocEnabled()).isFalse();
        }

        @Test
        void isIntervenerEnabledReturnsFalse() {
            assertThat(featureToggleService.isIntervenerEnabled()).isFalse();
        }

        @Test
        void isCaseFileViewEnabledReturnsFalse() {
            assertThat(featureToggleService.isCaseFileViewEnabled()).isFalse();
        }

        @Test
        void isExpressPilotEnabledReturnsFalse() {
            assertThat(featureToggleService.isExpressPilotEnabled()).isFalse();
        }

        @Test
        void isManageHearingEnabledReturnsFalse() {
            assertThat(featureToggleService.isManageHearingEnabled()).isFalse();
        }

        @Test
        void isVacateHearingEnabledReturnsFalse() {
            assertThat(featureToggleService.isVacateHearingEnabled()).isFalse();
        }

        @Test
        public void isFinremCitizenUiEnabled() {
            assertThat(featureToggleService.isFinremCitizenUiEnabled()).isFalse();
        }
    }
}
