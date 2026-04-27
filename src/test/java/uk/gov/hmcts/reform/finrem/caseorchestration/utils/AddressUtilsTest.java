package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddressUtils")
class AddressUtilsTest {

    @Nested
    @DisplayName("hasChange")
    class HasChange {

        @ParameterizedTest(name = "testAddressFieldChangeScenarios {0} has changed")
        @MethodSource()
        void testAddressFieldChangeScenarios(String fieldChange, Address a1, Address a2, boolean expected) {
            assertThat(AddressUtils.hasChange(a1, a2)).isEqualTo(expected);
        }

        static Stream<Arguments> testAddressFieldChangeScenarios() {
            return Stream.of(
                //  Address line1 Whitespace
                Arguments.of("Addresses Same",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    false),
                //  Address line1 Whitespace
                Arguments.of("Address Whitespace",
                    buildAddress("  123 Main St  ", "  Apt 4  ", "  Block B  ", "  County  ", "  UK  ", "  London  ", "  E1 1AA  "),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    false),
                //  Address both Null
                Arguments.of("Address both Null",
                    null,
                    null,
                    false),
                // Address after is Null
                Arguments.of("Address after is Null",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    null,
                    true),
                // Address after is Null
                Arguments.of("Address before is Null",
                    null,
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line1
                Arguments.of("AddressLine1",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("124 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line1
                Arguments.of("AddressLine1",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("124 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line1
                Arguments.of("AddressLine1",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("124 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line2
                Arguments.of("AddressLine2",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 5", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line3
                Arguments.of("AddressLine3",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block C", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address country
                Arguments.of("County",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "OtherCounty", "UK", "London", "E1 1AA"),
                    true),
                //  Address country
                Arguments.of("Country",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "France", "London", "E1 1AA"),
                    true),
                //  Address post town
                Arguments.of("PostTown",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "Manchester", "E1 1AA"),
                    true),
                //  Address post code
                Arguments.of("PostCode",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E2 2BB"),
                    true),
                //  Address line1 case
                Arguments.of("AddressLine1 Case",
                    buildAddress("main street", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("MAIN STREET", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line2 case
                Arguments.of("AddressLine2 Case",
                    buildAddress("123 Main St", "apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "APT 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line3 case
                Arguments.of("AddressLine3 Case",
                    buildAddress("123 Main St", "Apt 4", "block b", "County", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "BLOCK B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address county case
                Arguments.of("County Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "county", "UK", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "COUNTY", "UK", "London", "E1 1AA"),
                    true),
                //  Address country case
                Arguments.of("Country Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "uk", "London", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address post town case
                Arguments.of("PostTown Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "london", "E1 1AA"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "LONDON", "E1 1AA"),
                    true),
                //  Address post code case
                Arguments.of("PostCode Case",
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "e1 1aa"),
                    buildAddress("123 Main St", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    true),
                //  Address line1 null vs blank
                Arguments.of("AddressLine1 null AddressLine1 Blank Space",
                    buildAddress(null, "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    buildAddress(" ", "Apt 4", "Block B", "County", "UK", "London", "E1 1AA"),
                    false
                ),
                //  Address line1 null vs value
                Arguments.of("AddressLine1 null AddressLine1 value",
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
