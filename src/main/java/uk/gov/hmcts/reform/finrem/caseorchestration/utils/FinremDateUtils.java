package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class FinremDateUtils {

    public static LocalDateTime getLocalDateTime(String dateInString) {
        if (dateInString.contains("+")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(dateInString, dateTimeFormatter);
            return odtInstanceAtOffset.toLocalDateTime();
        } else {
            return LocalDateTime.parse(dateInString);
        }
    }
}
