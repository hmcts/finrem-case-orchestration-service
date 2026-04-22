package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

public class EmailTemplateResolver {

    private EmailTemplateResolver() {
    }

    public static EmailTemplateNames getNotifyApplicantRepresentativeTemplateName(FinremCaseData finremCaseData) {
        return finremCaseData.isContestedApplication()
            ? EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT
            : EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
    }

    public static EmailTemplateNames getNotifyRespondentRepresentativeTemplateName(FinremCaseData finremCaseData) {
        return finremCaseData.isContestedApplication()
            ? EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT
            : EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
    }

    public static EmailTemplateNames getNotifyIntervenerRepresentativeTemplateName(FinremCaseData finremCaseData) {
        return finremCaseData.isContestedApplication()
            ? EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER
            : EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER;
    }
}
