package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import static java.util.Objects.isNull;

/**
 * Utility class for comparing Address objects.
 */
public class AddressUtils {

    private AddressUtils() {
        /* This utility class should not be instantiated */
    }

    private static final String[] ADDRESS_FIELDS = {
        "addressLine1",
        "addressLine2",
        "addressLine3",
        "county",
        "country",
        "postTown",
        "postCode"
    };

    /**
     * Checks if any field in the address has changed between two Address objects.
     * Nulls and blanks are treated as empty strings, and comparison is case-insensitive and trimmed.
     * Applicant and Respondent Address are not requested when represented by solicitor so null's are allowed.
     *
     * @param oldAddress the original address
     * @param newAddress the new address
     * @return true if any field has changed, false otherwise
     */
    public static boolean hasChange(Address oldAddress, Address newAddress) {
        // Applicant or Respondent details if they are both represented by Solicitors can be null,
        // so we should not consider that as a change
        if (isNull(oldAddress) && isNull(newAddress)) {
            return false;
        }
        if ((isNull(oldAddress) && !isNull(newAddress)) || (!isNull(oldAddress) && isNull(newAddress))) {
            return true;
        }

        for (String field : ADDRESS_FIELDS) {
            if (!equals(getAddressFieldValue(oldAddress, field), getAddressFieldValue(newAddress, field))) {
                return true;
            }
        }
        return false;
    }

    private static String getAddressFieldValue(Address address, String fieldName) {
        return switch (fieldName) {
            case "addressLine1" -> address.getAddressLine1();
            case "addressLine2" -> address.getAddressLine2();
            case "addressLine3" -> address.getAddressLine3();
            case "county" -> address.getCounty();
            case "country" -> address.getCountry();
            case "postTown" -> address.getPostTown();
            case "postCode" -> address.getPostCode();
            default -> throw new IllegalArgumentException("Unknown address field: " + fieldName);
        };
    }

    private static boolean equals(String s1, String s2) {
        return StringUtils.defaultString(s1).trim().equals(StringUtils.defaultString(s2).trim());
    }
}
