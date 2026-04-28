package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class ContactDetailsWrapperTest {

    @ParameterizedTest
    @MethodSource("applicantAddressDetailFieldChanges")
    void shouldDetectChangesInApplicantAddressDetails(
        String fieldName,
        UnaryOperator<ContactDetailsWrapper.ContactDetailsWrapperBuilder> fieldChange
    ) {
        // Given
        ContactDetailsWrapper originalWrapper = applicantAddressDetailsWrapperBuilder().build();
        ContactDetailsWrapper modifiedWrapper = fieldChange.apply(applicantAddressDetailsWrapperBuilder()).build();

        // When
        boolean result = ContactDetailsWrapper.hasApplicantAddressDetailsChanged(originalWrapper, modifiedWrapper);

        // Then
        assertThat(result)
            .as("Expected change to applicant address detail field '%s' to be detected", fieldName)
            .isTrue();
    }

    @ParameterizedTest
    @MethodSource("applicantAddressDetailFieldChanges")
    void shouldNotDetectChangesWhenApplicantAddressDetailsAreSame(
        String fieldName,
        UnaryOperator<ContactDetailsWrapper.ContactDetailsWrapperBuilder> fieldChange
    ) {
        // Given
        ContactDetailsWrapper originalWrapper = fieldChange.apply(applicantAddressDetailsWrapperBuilder()).build();
        ContactDetailsWrapper identicalWrapper = fieldChange.apply(applicantAddressDetailsWrapperBuilder()).build();

        // When
        boolean result = ContactDetailsWrapper.hasApplicantAddressDetailsChanged(originalWrapper, identicalWrapper);

        // Then
        assertThat(result)
            .as("Expected identical applicant address detail field '%s' not to be detected as changed", fieldName)
            .isFalse();
    }

    private static Stream<Arguments> applicantAddressDetailFieldChanges() {
        return Stream.of(
            Arguments.of("applicantFmName", fieldChange(
                builder -> builder.applicantFmName("Jane")
            )),
            Arguments.of("applicantLname", fieldChange(
                builder -> builder.applicantLname("Smith")
            )),
            Arguments.of("applicantAddress", fieldChange(
                builder -> builder.applicantAddress(new Address(
                    "Changed Line1",
                    "Changed Line2",
                    "Changed Line3",
                    "Changed Town",
                    "Changed County",
                    "Changed PostCode",
                    "Changed Country"
                ))
            )),
            Arguments.of("applicantAddressConfidential", fieldChange(
                builder -> builder.applicantAddressHiddenFromRespondent(YesOrNo.YES)
            )),
            Arguments.of("applicantSolicitorName", fieldChange(
                builder -> builder.applicantSolicitorName("Changed Applicant Solicitor")
            )),
            Arguments.of("applicantSolicitorAddress", fieldChange(
                builder -> builder.applicantSolicitorAddress(new Address(
                    "Changed Solicitor Line1",
                    "Changed Solicitor Line2",
                    "Changed Solicitor Line3",
                    "Changed Solicitor Town",
                    "Changed Solicitor County",
                    "Changed Solicitor PostCode",
                    "Changed Solicitor Country"
                ))
            ))
        );
    }

    @ParameterizedTest
    @MethodSource("respondentAddressDetailFieldChanges")
    void shouldDetectChangesInRespondentAddressDetails(
        String fieldName,
        UnaryOperator<ContactDetailsWrapper.ContactDetailsWrapperBuilder> fieldChange
    ) {
        // Given
        ContactDetailsWrapper originalWrapper = respondentAddressDetailsWrapperBuilder().build();
        ContactDetailsWrapper modifiedWrapper = fieldChange.apply(respondentAddressDetailsWrapperBuilder()).build();

        // When
        boolean result = ContactDetailsWrapper.hasRespondentAddressDetailsChanged(originalWrapper, modifiedWrapper);

        // Then
        assertThat(result)
            .as("Expected change to respondent address detail field '%s' to be detected", fieldName)
            .isTrue();
    }

    @ParameterizedTest
    @MethodSource("respondentAddressDetailFieldChanges")
    void shouldNotDetectChangesWhenRespondentAddressDetailsAreSame(
        String fieldName,
        UnaryOperator<ContactDetailsWrapper.ContactDetailsWrapperBuilder> fieldChange
    ) {
        // Given
        ContactDetailsWrapper originalWrapper = fieldChange.apply(respondentAddressDetailsWrapperBuilder()).build();
        ContactDetailsWrapper identicalWrapper = fieldChange.apply(respondentAddressDetailsWrapperBuilder()).build();

        // When
        boolean result = ContactDetailsWrapper.hasRespondentAddressDetailsChanged(originalWrapper, identicalWrapper);

        // Then
        assertThat(result)
            .as("Expected identical respondent address detail field '%s' not to be detected as changed", fieldName)
            .isFalse();
    }

    private static Stream<Arguments> respondentAddressDetailFieldChanges() {
        return Stream.of(
            Arguments.of("respondentFmName", fieldChange(
                builder -> builder.respondentFmName("Jane")
            )),
            Arguments.of("respondentLname", fieldChange(
                builder -> builder.respondentLname("Smith")
            )),
            Arguments.of("respondentAddress", fieldChange(
                builder -> builder.respondentAddress(new Address(
                    "Changed Line1",
                    "Changed Line2",
                    "Changed Line3",
                    "Changed Town",
                    "Changed County",
                    "Changed PostCode",
                    "Changed Country"
                ))
            )),
            Arguments.of("respondentAddressHiddenFromApplicant", fieldChange(
                builder -> builder.respondentAddressHiddenFromApplicant(YesOrNo.YES)
            )),
            Arguments.of("respondentSolicitorName", fieldChange(
                builder -> builder.respondentSolicitorName("Changed Respondent Solicitor")
            )),
            Arguments.of("respondentSolicitorAddress", fieldChange(
                builder -> builder.respondentSolicitorAddress(new Address(
                    "Changed Solicitor Line1",
                    "Changed Solicitor Line2",
                    "Changed Solicitor Line3",
                    "Changed Solicitor Town",
                    "Changed Solicitor County",
                    "Changed Solicitor PostCode",
                    "Changed Solicitor Country"
                ))
            ))
        );
    }

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

    private static ContactDetailsWrapper.ContactDetailsWrapperBuilder applicantAddressDetailsWrapperBuilder() {
        return ContactDetailsWrapper.builder()
            .applicantFmName("John")
            .applicantLname("Doe")
            .applicantAddress(new Address("Line1", "Line2", "Line3", "Town", "County", "PostCode", "Country"))
            .applicantAddressHiddenFromRespondent(YesOrNo.NO)
            .applicantSolicitorName("Applicant Solicitor")
            .applicantSolicitorAddress(new Address(
                "Solicitor Line1",
                "Solicitor Line2",
                "Solicitor Line3",
                "Solicitor Town",
                "Solicitor County",
                "Solicitor PostCode",
                "Solicitor Country"
            ));
    }

    private static ContactDetailsWrapper.ContactDetailsWrapperBuilder respondentAddressDetailsWrapperBuilder() {
        return ContactDetailsWrapper.builder()
            .respondentFmName("John")
            .respondentLname("Doe")
            .respondentAddress(new Address("Line1", "Line2", "Line3", "Town", "County", "PostCode", "Country"))
            .respondentAddressHiddenFromApplicant(YesOrNo.NO)
            .respondentSolicitorName("Respondent Solicitor")
            .respondentSolicitorAddress(new Address(
                "Solicitor Line1",
                "Solicitor Line2",
                "Solicitor Line3",
                "Solicitor Town",
                "Solicitor County",
                "Solicitor PostCode",
                "Solicitor Country"
            ));
    }

    private static UnaryOperator<ContactDetailsWrapper.ContactDetailsWrapperBuilder> fieldChange(
        UnaryOperator<ContactDetailsWrapper.ContactDetailsWrapperBuilder> fieldChange
    ) {
        return fieldChange;
    }
}
