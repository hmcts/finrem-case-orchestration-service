package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

/**
 * Utility class for comparing Address objects.
 */
@UtilityClass
public class AddressUtils {
    /**
     * Checks if any field in the address has changed between two Address objects.
     * Nulls and blanks are treated as empty strings, and comparison is case-insensitive and trimmed.
     *
     * @param oldAddress the original address
     * @param newAddress the new address
     * @return true if any field has changed, false otherwise
     */
    public static boolean hasChange(Address oldAddress, Address newAddress) {
        if (oldAddress == newAddress) {
            return false;
        }
        if (oldAddress == null || newAddress == null) {
            return true;
        }
        return !equalsIgnoreCaseAndTrim(oldAddress.getAddressLine1(), newAddress.getAddressLine1())
            || !equalsIgnoreCaseAndTrim(oldAddress.getAddressLine2(), newAddress.getAddressLine2())
            || !equalsIgnoreCaseAndTrim(oldAddress.getAddressLine3(), newAddress.getAddressLine3())
            || !equalsIgnoreCaseAndTrim(oldAddress.getCounty(), newAddress.getCounty())
            || !equalsIgnoreCaseAndTrim(oldAddress.getCountry(), newAddress.getCountry())
            || !equalsIgnoreCaseAndTrim(oldAddress.getPostTown(), newAddress.getPostTown())
            || !equalsIgnoreCaseAndTrim(oldAddress.getPostCode(), newAddress.getPostCode());
    }

    private static boolean equalsIgnoreCaseAndTrim(String s1, String s2) {
        return StringUtils.defaultString(s1).trim().equalsIgnoreCase(StringUtils.defaultString(s2).trim());
    }
}

