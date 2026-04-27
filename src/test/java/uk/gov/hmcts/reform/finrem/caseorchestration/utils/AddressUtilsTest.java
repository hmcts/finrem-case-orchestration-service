package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AddressUtils")
class AddressUtilsTest {

    @Nested
    @DisplayName("hasChange")
    class HasChange {

        @Test
        void shouldReturnFalseWhenFieldsContainsWhitespace() {
            Address a1 = Address.builder().addressLine1("123 Main St ").build();
            Address a2 = Address.builder().addressLine1("123 Main St").build();
            assertFalse(AddressUtils.hasChange(a1, a2));
        }

        @Test
        void shouldReturnFalseIfBothAddressesAreNull() {
            assertFalse(AddressUtils.hasChange(null, null));
        }

        @ParameterizedTest(name = "shouldReturnTrueWhen {0} IsDifferent")
        @MethodSource("addressFieldChangeProvider")
        void shouldReturnTrueWhenFieldIsDifferent(String ignoredField, Address a1, Address a2) {
            assertTrue(AddressUtils.hasChange(a1, a2));
        }

        static Stream<Arguments> addressFieldChangeProvider() {
            return Stream.of(
                //  Address line1
                Arguments.of("AddressLine1",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("124 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA")),
                //  Address line2
                Arguments.of("AddressLine2",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 5", "Block B", "County", "UK", "London", "E1 1AA")),
                //  Address line3
                Arguments.of("AddressLine3",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block C", "County", "UK", "London", "E1 1AA")),
                //  Address country
                Arguments.of("County",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "OtherCounty", "UK", "London", "E1 1AA")),
                //  Address country
                Arguments.of("Country",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "France", "London", "E1 1AA")),
                //  Address post town
                Arguments.of("PostTown",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "Manchester", "E1 1AA")),
                //  Address post code
                Arguments.of("PostCode",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E2 2BB")),
                //  Address line1 case
                Arguments.of("AddressLine1 Case",
                    buildAddress("main street", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("MAIN STREET", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA")),
                //  Address line2 case
                Arguments.of("AddressLine2 Case",
                    buildAddress("123 Main St", "apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "APT 4", "Block B", "County", "UK", "London", "E1 1AA")),
                //  Address line3 case
                Arguments.of("AddressLine3 Case",
                    buildAddress("123 Main St", "Apt 4", "block b", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "BLOCK B", "County", "UK", "London", "E1 1AA")),
                //  Address county case
                Arguments.of("County Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "county", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "COUNTY", "UK", "London", "E1 1AA")),
                //  Address country case
                Arguments.of("Country Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "uk", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA")),
                //  Address post town case
                Arguments.of("PostTown Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "london", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "LONDON", "E1 1AA")),
                //  Address post code case
                Arguments.of("PostCode Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "e1 1aa"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"))
            );
        }

        @ParameterizedTest
        @MethodSource("addressWhitespaceProvider")
        void shouldReturnFalseWhenFieldsContainOnlyWhitespace(Address a1, Address a2) {
            assertFalse(AddressUtils.hasChange(a1, a2));
        }

        static Stream<Arguments> addressWhitespaceProvider() {
            return Stream.of(
                Arguments.of(
                    buildAddress("123 Main St ", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA")
                ),
                Arguments.of(
                    buildAddress("123 Main St", "Apt 4 ", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA")
                )
            );
        }

        @Test
        void shouldReturnFalseWhenAddressesAreSame() {
            Address a1 = buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA");
            Address a2 = buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA");
            assertFalse(AddressUtils.hasChange(a1, a2));
        }

        @Test
        void shouldReturnTrueIfOneAddressIsNull() {
            Address a1 = buildAddress("A", null, null, null, null, null, null);
            assertTrue(AddressUtils.hasChange(a1, null));
            assertTrue(AddressUtils.hasChange(null, a1));
        }

        @ParameterizedTest
        @MethodSource("addressNullFieldProvider")
        void shouldReturnTrueIfOneFieldIsNullAndOtherIsNot(Address a1, Address a2, boolean expected) {
            if (expected) {
                assertTrue(AddressUtils.hasChange(a1, a2));
            } else {
                assertFalse(AddressUtils.hasChange(a1, a2));
            }
        }

        static Stream<Arguments> addressNullFieldProvider() {
            return Stream.of(
                Arguments.of(
                    buildAddress(null, "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress(" ", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    false
                ),
                Arguments.of(
                    buildAddress(null, "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("A", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true
                )
            );
        }

        private static Address buildAddress(String line1, String line2, String line3, String county,
                                            String country, String postTown, String postCode) {
            return Address.builder()
                .addressLine1(line1)
                .addressLine2(line2)
                .addressLine3(line3)
                .county(county)
                .country(country)
                .postTown(postTown)
                .postCode(postCode)
                .build();
        }
    }
}
