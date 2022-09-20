package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class FinremDateUtils {

    public static LocalDateTime getLocalDateTime(String dateInString) {
        if(dateInString.contains("+")) {
            DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(dateInString, DATE_TIME_FORMATTER);
            return odtInstanceAtOffset.toLocalDateTime();
        }else{
            return LocalDateTime.parse(dateInString);
        }
    }
}
