package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;

class EmailTemplateNamesTest {

    @Test
    void givenJvmIsWorkingAccordingToSpecification_whenEnumNameInvoked_thenItReturnsSameStringAsEnumName() {
        ImmutableMap.<EmailTemplateNames, String>builder()
            // consented
            .put(FR_HWF_SUCCESSFUL, "FR_HWF_SUCCESSFUL")
            .put(FR_ASSIGNED_TO_JUDGE, "FR_ASSIGNED_TO_JUDGE")
            .put(FR_CONSENT_ORDER_MADE, "FR_CONSENT_ORDER_MADE")
            .put(FR_CONSENT_ORDER_NOT_APPROVED, "FR_CONSENT_ORDER_NOT_APPROVED")
            .put(FR_CONSENTED_GENERAL_ORDER, "FR_CONSENTED_GENERAL_ORDER")
            .put(FR_CONSENT_GENERAL_EMAIL, "FR_CONSENT_GENERAL_EMAIL")
            .put(FR_TRANSFER_TO_LOCAL_COURT, "FR_TRANSFER_TO_LOCAL_COURT")
            // contested
            .put(FR_CONTESTED_DRAFT_ORDER, "FR_CONTESTED_DRAFT_ORDER")
            .put(FR_CONTESTED_HWF_SUCCESSFUL, "FR_CONTESTED_HWF_SUCCESSFUL")
            .put(FR_CONTESTED_APPLICATION_ISSUED, "FR_CONTESTED_APPLICATION_ISSUED")
            .put(FR_CONTESTED_PREPARE_FOR_HEARING, "FR_CONTESTED_PREPARE_FOR_HEARING")
            .put(FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL, "FR_CONTESTED_PREPARE_FOR_HEARING_INTERVENER_SOL")
            .put(FR_CONTEST_ORDER_APPROVED_INTERVENER1, "FR_CONTEST_ORDER_APPROVED_INTERVENER1")
            .put(FR_CONTEST_ORDER_APPROVED_INTERVENER2, "FR_CONTEST_ORDER_APPROVED_INTERVENER2")
            .put(FR_CONTEST_ORDER_APPROVED_INTERVENER3, "FR_CONTEST_ORDER_APPROVED_INTERVENER3")
            .put(FR_CONTEST_ORDER_APPROVED_INTERVENER4, "FR_CONTEST_ORDER_APPROVED_INTERVENER4")
            .put(FR_CONTEST_ORDER_APPROVED_APPLICANT, "FR_CONTEST_ORDER_APPROVED_APPLICANT")
            .put(FR_CONTEST_ORDER_APPROVED_RESPONDENT, "FR_CONTEST_ORDER_APPROVED_RESPONDENT")
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
            .forEach((key, value) -> assertThat(key.name(), is(value)));
    }
}
