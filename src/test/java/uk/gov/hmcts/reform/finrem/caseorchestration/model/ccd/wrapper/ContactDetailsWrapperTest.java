package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
        contactDetails.setRespondentSolicitorAddress(Address.builder().country("United Kingdom").build());
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
}
