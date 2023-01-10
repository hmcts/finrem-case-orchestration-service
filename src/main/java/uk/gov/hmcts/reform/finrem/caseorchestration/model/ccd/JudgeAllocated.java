package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum JudgeAllocated {
    FR_JUDGE_ALLOCATED_LIST_1("FR_judgeAllocatedList_1"),
    FR_JUDGE_ALLOCATED_LIST_2("FR_judgeAllocatedList_2"),
    FR_JUDGE_ALLOCATED_LIST_3("FR_judgeAllocatedList_3");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static JudgeAllocated forValue(String value) {
        return Arrays.stream(JudgeAllocated.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
