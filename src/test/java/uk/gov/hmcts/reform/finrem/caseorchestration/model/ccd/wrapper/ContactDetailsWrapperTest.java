package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
}
