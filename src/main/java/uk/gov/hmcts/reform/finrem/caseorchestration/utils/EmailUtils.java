package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

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
}
