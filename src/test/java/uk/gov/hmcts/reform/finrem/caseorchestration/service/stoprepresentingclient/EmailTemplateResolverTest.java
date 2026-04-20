package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EmailTemplateResolverTest {

    @Test
    void shouldReturnContestedApplicantTemplate_whenApplicationIsContested() {
        FinremCaseData caseData = Mockito.mock(FinremCaseData.class);
        when(caseData.isContestedApplication()).thenReturn(true);

        EmailTemplateNames result =
            EmailTemplateResolver.getNotifyApplicantRepresentativeTemplateName(caseData);

        assertThat(result)
            .isEqualTo(EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT);
    }

    @Test
    void shouldReturnConsentedApplicantTemplate_whenApplicationIsNotContested() {
        FinremCaseData caseData = Mockito.mock(FinremCaseData.class);
        when(caseData.isContestedApplication()).thenReturn(false);

        EmailTemplateNames result =
            EmailTemplateResolver.getNotifyApplicantRepresentativeTemplateName(caseData);

        assertThat(result)
            .isEqualTo(EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT);
    }

    @Test
    void shouldReturnContestedRespondentTemplate_whenApplicationIsContested() {
        FinremCaseData caseData = Mockito.mock(FinremCaseData.class);
        when(caseData.isContestedApplication()).thenReturn(true);

        EmailTemplateNames result =
            EmailTemplateResolver.getNotifyRespondentRepresentativeTemplateName(caseData);

        assertThat(result)
            .isEqualTo(EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT);
    }

    @Test
    void shouldReturnConsentedRespondentTemplate_whenApplicationIsNotContested() {
        FinremCaseData caseData = Mockito.mock(FinremCaseData.class);
        when(caseData.isContestedApplication()).thenReturn(false);

        EmailTemplateNames result =
            EmailTemplateResolver.getNotifyRespondentRepresentativeTemplateName(caseData);

        assertThat(result)
            .isEqualTo(EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT);
    }

    @Test
    void shouldReturnContestedIntervenerTemplate_whenApplicationIsContested() {
        FinremCaseData caseData = Mockito.mock(FinremCaseData.class);
        when(caseData.isContestedApplication()).thenReturn(true);

        EmailTemplateNames result =
            EmailTemplateResolver.getNotifyIntervenerRepresentativeTemplateName(caseData);

        assertThat(result)
            .isEqualTo(EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER);
    }

    @Test
    void shouldReturnConsentedIntervenerTemplate_whenApplicationIsNotContested() {
        FinremCaseData caseData = Mockito.mock(FinremCaseData.class);
        when(caseData.isContestedApplication()).thenReturn(false);

        EmailTemplateNames result =
            EmailTemplateResolver.getNotifyIntervenerRepresentativeTemplateName(caseData);

        assertThat(result)
            .isEqualTo(EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER);
    }
}
