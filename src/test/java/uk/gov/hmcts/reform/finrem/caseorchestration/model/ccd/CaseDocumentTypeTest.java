package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseDocumentTypeTest {

    @Test
    void shouldIdentifyCitizenFormCDocumentTypes() {
        assertThat(CaseDocumentType.isCitizenFormCDocumentType(CaseDocumentType.FORM_C_APPLICANT_CITIZEN)).isTrue();
        assertThat(CaseDocumentType.isCitizenFormCDocumentType(CaseDocumentType.FORM_C_FAST_TRACK_RESPONDENT_CITIZEN)).isTrue();
        assertThat(CaseDocumentType.isCitizenFormCDocumentType(CaseDocumentType.FORM_C)).isFalse();
        assertThat(CaseDocumentType.isCitizenFormCDocumentType(null)).isFalse();
    }

    @Test
    void shouldIdentifyDefaultFormCDocumentTypes() {
        assertThat(CaseDocumentType.isDefaultFormCDocumentType(CaseDocumentType.FORM_C)).isTrue();
        assertThat(CaseDocumentType.isDefaultFormCDocumentType(CaseDocumentType.FORM_C_FAST_TRACK)).isTrue();
        assertThat(CaseDocumentType.isDefaultFormCDocumentType(CaseDocumentType.FORM_C_EXPRESS)).isTrue();
        assertThat(CaseDocumentType.isDefaultFormCDocumentType(CaseDocumentType.FORM_C_APPLICANT_CITIZEN)).isFalse();
        assertThat(CaseDocumentType.isDefaultFormCDocumentType(null)).isFalse();
    }

    @Test
    void shouldMapStandardFormCToApplicantCitizenEquivalent() {
        assertThat(CaseDocumentType.getApplicantCitizenFormCType(CaseDocumentType.FORM_C))
            .isEqualTo(CaseDocumentType.FORM_C_APPLICANT_CITIZEN);
        assertThat(CaseDocumentType.getApplicantCitizenFormCType(CaseDocumentType.FORM_C_FAST_TRACK))
            .isEqualTo(CaseDocumentType.FORM_C_FAST_TRACK_APPLICANT_CITIZEN);
        assertThat(CaseDocumentType.getApplicantCitizenFormCType(CaseDocumentType.FORM_C_EXPRESS))
            .isEqualTo(CaseDocumentType.FORM_C_EXPRESS_APPLICANT_CITIZEN);
    }

    @Test
    void shouldMapStandardFormCToRespondentCitizenEquivalent() {
        assertThat(CaseDocumentType.getRespondentCitizenFormCType(CaseDocumentType.FORM_C))
            .isEqualTo(CaseDocumentType.FORM_C_RESPONDENT_CITIZEN);
        assertThat(CaseDocumentType.getRespondentCitizenFormCType(CaseDocumentType.FORM_C_FAST_TRACK))
            .isEqualTo(CaseDocumentType.FORM_C_FAST_TRACK_RESPONDENT_CITIZEN);
        assertThat(CaseDocumentType.getRespondentCitizenFormCType(CaseDocumentType.FORM_C_EXPRESS))
            .isEqualTo(CaseDocumentType.FORM_C_EXPRESS_RESPONDENT_CITIZEN);
    }

    @Test
    void shouldResolveCaseDocumentTypeFromLabel() {
        assertThat(CaseDocumentType.getCaseDocumentType("Form C - Express - Applicant Citizen"))
            .isEqualTo(CaseDocumentType.FORM_C_EXPRESS_APPLICANT_CITIZEN);
    }

    @Test
    void shouldThrowWhenCaseDocumentTypeLabelUnknown() {
        assertThatThrownBy(() -> CaseDocumentType.getCaseDocumentType("unknown-type"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
