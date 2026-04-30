package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Objects;

import static java.util.Optional.ofNullable;

@Slf4j
public class EmailUtils {

    private EmailUtils() {
    }

    /**
     * Validates whether the given email address is in a valid format.
     *
     * <p>
     * This method delegates to {@link #isValidEmailAddress(String, boolean)} with {@code allowEmpty} set to {@code false}.
     * It returns {@code true} if the email format is valid according to {@link org.apache.commons.validator.routines.EmailValidator};
     * otherwise, it returns {@code false}.
     * </p>
     *
     * @param email the email address to validate
     * @return {@code true} if the email address is valid; {@code false} otherwise
     */
    public static boolean isValidEmailAddress(final String email) {
        return isValidEmailAddress(email, false);
    }

    public static boolean isValidEmailAddress(final String email, boolean allowEmpty) {
        if (allowEmpty && StringUtils.isEmpty(email)) {
            return true;
        }
        return EmailValidator.getInstance().isValid(email);
    }

    /**
     * Compares two email addresses to determine whether they are different,
     * ignoring case, leading/trailing whitespace, and treating blank values as null.
     *
     * <p>Both email inputs are normalised before comparison by:
     * <ul>
     *     <li>Trimming leading and trailing whitespace</li>
     *     <li>Converting to lower case</li>
     *     <li>Converting empty strings to null</li>
     * </ul>
     *
     * @param email1 the first email address to compare; may be null or blank
     * @param email2 the second email address to compare; may be null or blank
     * @return {@code true} if the normalised email addresses are different, {@code false} otherwise
     */
    public static boolean areEmailsDifferent(String email1, String email2) {
        return !Objects.equals(normaliseAndLower(email1), normaliseAndLower(email2));
    }

    private static String normaliseAndLower(String email) {
        return ofNullable(email)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .orElse(null);
    }
}
