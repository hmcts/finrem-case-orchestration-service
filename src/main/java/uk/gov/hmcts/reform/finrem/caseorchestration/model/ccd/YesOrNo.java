package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public enum YesOrNo {
    YES("Yes"),
    NO("No");

    private final String value;

    @JsonValue
    public String getYesOrNo() {
        return value;
    }

    public static String getYesOrNo(YesOrNo answer) {
        return isYes(answer)
            ? YesOrNo.YES.getYesOrNo()
            : YesOrNo.NO.getYesOrNo();
    }

    public boolean isYes() {
        return YES.getYesOrNo().equalsIgnoreCase(value);
    }

    public static boolean isYes(YesOrNo yesOrNo) {
        return YES.equals(yesOrNo);
    }

    public static boolean isYes(String yesOrNo) {
        return YES.toString().equalsIgnoreCase(yesOrNo);
    }

    public static boolean isNoOrNull(YesOrNo yesOrNo) {
        return isNull(yesOrNo) || NO.equals(yesOrNo);
    }

    public static boolean isNoOrNull(String yesOrNo) {
        return isNull(yesOrNo) || NO.toString().equalsIgnoreCase(yesOrNo);
    }

    public boolean isNoOrNull() {
        return isNull(value) || NO.getYesOrNo().equalsIgnoreCase(value);
    }

    public static boolean isNo(YesOrNo yesOrNo) {
        return NO.equals(yesOrNo);
    }

    public static boolean isNo(String yesOrNo) {
        return NO.getYesOrNo().equalsIgnoreCase(yesOrNo);
    }

    public static YesOrNo forValue(String yesOrNo) {
        return Stream.of(YesOrNo.values())
            .filter(value -> value.getYesOrNo().equalsIgnoreCase(yesOrNo))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static YesOrNo forValue(boolean b) {
        return forValue(b ? YES.value : NO.value);
    }
}
