package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;

@Slf4j
public class EmailUtils {

    private EmailUtils() {
    }

    /**
     * Validates whether the given email address is in a valid format.
     *
     * <p>
     * This method attempts to create and validate a {@link javax.mail.internet.InternetAddress}
     * using the provided email string. If the email format is valid, it returns {@code true};
     * otherwise, it returns {@code false}.
     * </p>
     *
     * @param email the email address to validate
     * @return {@code true} if the email address is valid; {@code false} otherwise
     */
    public static boolean isValidEmailAddress(final String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
