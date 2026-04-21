package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AddressUtils")
class AddressUtilsTest {

    @Nested
    @DisplayName("hasChange")
    class HasChange {
        @Test
        void shouldReturnFalseWhenAddressesAreSame() {
            Address a1 = Address.builder()
                    .addressLine1("123 Main St")
                    .addressLine2("Apt 4")
                    .addressLine3("Block B")
                    .county("County")
                    .country("UK")
                    .postTown("London")
                    .postCode("E1 1AA")
                    .build();
            Address a2 = Address.builder()
                    .addressLine1("123 Main St")
                    .addressLine2("Apt 4")
                    .addressLine3("Block B")
                    .county("County")
                    .country("UK")
                    .postTown("London")
                    .postCode("E1 1AA")
                    .build();
            assertFalse(AddressUtils.hasChange(a1, a2));
        }

        @Test
        void shouldReturnTrueWhenAnyFieldIsDifferent() {
            Address a1 = Address.builder().addressLine1("123 Main St").build();
            Address a2 = Address.builder().addressLine1("124 Main St").build();
            assertTrue(AddressUtils.hasChange(a1, a2));
        }

        @Test
        void shouldReturnTrueWhenFieldsDifferOnlyInCaseOrWhitespace() {
            Address a1 = Address.builder().addressLine1(" 123 main st ").build();
            Address a2 = Address.builder().addressLine1("123 MAIN ST").build();
            assertTrue(AddressUtils.hasChange(a1, a2));
        }

        @Test
        void shouldReturnTrueIfOneAddressIsNull() {
            Address a1 = Address.builder().addressLine1("A").build();
            assertTrue(AddressUtils.hasChange(a1, null));
            assertTrue(AddressUtils.hasChange(null, a1));
        }

        @Test
        void shouldReturnFalseIfBothAddressesAreNull() {
            assertFalse(AddressUtils.hasChange(null, null));
        }

        @Test
        void shouldReturnTrueIfOneFieldIsNullAndOtherIsNot() {
            Address a1 = Address.builder().addressLine1(null).build();
            Address a2 = Address.builder().addressLine1(" ").build();
            assertFalse(AddressUtils.hasChange(a1, a2));
            Address a3 = Address.builder().addressLine1("A").build();
            assertTrue(AddressUtils.hasChange(a1, a3));
        }
    }
}

