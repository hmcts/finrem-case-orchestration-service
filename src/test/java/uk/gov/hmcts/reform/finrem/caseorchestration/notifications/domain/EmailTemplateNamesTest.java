package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;

public class EmailTemplateNamesTest {

    @Test
    public void givenJvmIsWorkingAccordingToSpecification_whenEnumNameInvoked_thenItReturnsSameStringAsEnumName() {
        ImmutableMap.<Enum, String>builder()
            // consented
            .put(FR_HWF_SUCCESSFUL, "FR_HWF_SUCCESSFUL")
            .put(FR_ASSIGNED_TO_JUDGE, "FR_ASSIGNED_TO_JUDGE")
            .put(FR_CONSENT_ORDER_MADE, "FR_CONSENT_ORDER_MADE")
            .put(FR_CONSENT_ORDER_NOT_APPROVED, "FR_CONSENT_ORDER_NOT_APPROVED")
            .put(FR_CONSENT_ORDER_NOT_APPROVED_SENT, "FR_CONSENT_ORDER_NOT_APPROVED_SENT")
            .put(FR_CONSENTED_GENERAL_ORDER, "FR_CONSENTED_GENERAL_ORDER")
            .put(FR_CONSENT_GENERAL_EMAIL, "FR_CONSENT_GENERAL_EMAIL")
            .put(FR_TRANSFER_TO_LOCAL_COURT, "FR_TRANSFER_TO_LOCAL_COURT")
            // contested
            .put(FR_CONSENT_ORDER_AVAILABLE, "FR_CONSENT_ORDER_AVAILABLE")
            .put(FR_CONTESTED_DRAFT_ORDER, "FR_CONTESTED_DRAFT_ORDER")
            .put(FR_CONTESTED_HWF_SUCCESSFUL, "FR_CONTESTED_HWF_SUCCESSFUL")
            .put(FR_CONTESTED_APPLICATION_ISSUED, "FR_CONTESTED_APPLICATION_ISSUED")
            .put(FR_CONTESTED_PREPARE_FOR_HEARING, "FR_CONTESTED_PREPARE_FOR_HEARING")
            .put(FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL, "FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL")
            .put(FR_CONTEST_ORDER_APPROVED, "FR_CONTEST_ORDER_APPROVED")
            .put(FR_CONTEST_ORDER_NOT_APPROVED, "FR_CONTEST_ORDER_NOT_APPROVED")
            .put(FR_CONTESTED_CONSENT_ORDER_APPROVED, "FR_CONTESTED_CONSENT_ORDER_APPROVED")
            .put(FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED, "FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED")
            .put(FR_CONTESTED_GENERAL_ORDER_CONSENT, "FR_CONTESTED_GENERAL_ORDER_CONSENT")
            .put(FR_CONTESTED_GENERAL_APPLICATION_OUTCOME, "FR_CONTESTED_GENERAL_APPLICATION_OUTCOME")
            .put(FR_CONTESTED_GENERAL_ORDER, "FR_CONTESTED_GENERAL_ORDER")
            .put(FR_CONTESTED_GENERAL_EMAIL, "FR_CONTESTED_GENERAL_EMAIL")
            .put(FR_CONTESTED_INTERIM_HEARING, "FR_CONTESTED_INTERIM_HEARING")
            .put(FR_CONSENTED_LIST_FOR_HEARING, "FR_CONSENTED_LIST_FOR_HEARING")
            .build()
            .entrySet()
            .forEach(enumAndName -> assertThat(enumAndName.getKey().name(), is(enumAndName.getValue())));
    }
}
