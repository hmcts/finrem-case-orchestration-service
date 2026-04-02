package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Objects;

@Slf4j
public class EmailUtils {

    private EmailUtils() {
    }

    /**
     * Validates whether the given email address is in a valid format.
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
     * Determines if two solicitor email addresses are different, ignoring case. Handles nulls and empty strings as equal.
     *
     * @param currentEmail  the current email address
     * @param previousEmail the previous email address
     * @return true if the emails are different (case-insensitive), false if they are the same or both null/empty
     */
    public static boolean hasSolicitorEmailChanged(String currentEmail, String previousEmail) {
        if (currentEmail == null || previousEmail == null) {
            return !Objects.equals(currentEmail, previousEmail);
        }
        return !currentEmail.equalsIgnoreCase(previousEmail);
    }
}
