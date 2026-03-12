package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class ContactDetailsWrapperTest {

    @Test
    void givenTwoWrappers_whenDiffCalled_thenReturnOnlyFieldsThatChanged() {
        // given
        ContactDetailsWrapper a = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Alice")
            .applicantEmail("alice@test.com")
            .respondentPhone(null)
            .build();

        ContactDetailsWrapper b = ContactDetailsWrapper.builder()
            .applicantSolicitorName("Bob")
            .applicantEmail("alice@test.com")
            .respondentPhone("")
            .build();

        // when
        Map<String, Object[]> result = ContactDetailsWrapper.diff(a, b);

        // then
        assertThat(result)
            .containsOnlyKeys("applicantSolicitorName");

        assertThat(result.get("applicantSolicitorName"))
            .containsExactly("Alice", "Bob");
    }

    @Test
    void shouldClearAllRespondentSolicitorFields() {
        ContactDetailsWrapper contactDetails = new ContactDetailsWrapper();
        contactDetails.setRespondentSolicitorName("John Doe");
        contactDetails.setRespondentSolicitorFirm("Firm Ltd");
        contactDetails.setRespondentSolicitorReference("REF123");
        contactDetails.setRespondentSolicitorAddress(mock(Address.class));
        contactDetails.setRespondentSolicitorPhone("0123456789");
        contactDetails.setRespondentSolicitorEmail("email@test.com");
        contactDetails.setRespondentSolicitorDxNumber("DX456");

        // Act
        contactDetails.clearRespondentSolicitorFields();

        // Assert all fields are null
        assertAll("respondent solicitor fields",
            () -> assertThat(contactDetails.getRespondentSolicitorName()).isNull(),
            () -> assertThat(contactDetails.getRespondentSolicitorFirm()).isNull(),
            () -> assertThat(contactDetails.getRespondentSolicitorReference()).isNull(),
            () -> assertThat(contactDetails.getRespondentSolicitorAddress()).isNull(),
            () -> assertThat(contactDetails.getRespondentSolicitorPhone()).isNull(),
            () -> assertThat(contactDetails.getRespondentSolicitorEmail()).isNull(),
            () -> assertThat(contactDetails.getRespondentSolicitorDxNumber()).isNull()
        );
    }

    @Test
    void shouldClearAllApplicantSolicitorFields() {
        ContactDetailsWrapper contactDetails = new ContactDetailsWrapper();

        // populate fields
        contactDetails.setSolicitorReference("REF123");
        contactDetails.setSolicitorName("John Doe");
        contactDetails.setSolicitorFirm("Firm Ltd");
        contactDetails.setSolicitorAddress(mock(Address.class));
        contactDetails.setSolicitorPhone("123456");
        contactDetails.setSolicitorEmail("email@test.com");
        contactDetails.setSolicitorDxNumber("DX123");
        contactDetails.setSolicitorAgreeToReceiveEmails(YesOrNo.YES);

        contactDetails.setApplicantSolicitorName("Jane Doe");
        contactDetails.setApplicantSolicitorFirm("Another Firm");
        contactDetails.setApplicantSolicitorAddress(mock(Address.class));
        contactDetails.setApplicantSolicitorPhone("987654");
        contactDetails.setApplicantSolicitorEmail("email2@test.com");
        contactDetails.setApplicantSolicitorDxNumber("DX456");
        contactDetails.setApplicantSolicitorConsentForEmails(YesOrNo.YES);

        // act
        contactDetails.clearApplicantSolicitorFields();

        // assert
        assertAll("applicant solicitor fields",
            () -> assertThat(contactDetails.getSolicitorReference()).isNull(),

            () -> assertThat(contactDetails.getSolicitorName()).isNull(),
            () -> assertThat(contactDetails.getSolicitorFirm()).isNull(),
            () -> assertThat(contactDetails.getSolicitorAddress()).isNull(),
            () -> assertThat(contactDetails.getSolicitorPhone()).isNull(),
            () -> assertThat(contactDetails.getSolicitorEmail()).isNull(),
            () -> assertThat(contactDetails.getSolicitorDxNumber()).isNull(),
            () -> assertThat(contactDetails.getSolicitorAgreeToReceiveEmails()).isNull(),

            () -> assertThat(contactDetails.getApplicantSolicitorName()).isNull(),
            () -> assertThat(contactDetails.getApplicantSolicitorFirm()).isNull(),
            () -> assertThat(contactDetails.getApplicantSolicitorAddress()).isNull(),
            () -> assertThat(contactDetails.getApplicantSolicitorPhone()).isNull(),
            () -> assertThat(contactDetails.getApplicantSolicitorEmail()).isNull(),
            () -> assertThat(contactDetails.getApplicantSolicitorDxNumber()).isNull(),
            () -> assertThat(contactDetails.getApplicantSolicitorConsentForEmails()).isNull()
        );
    }
}
