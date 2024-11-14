package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Data
public class SortKey implements Comparable<SortKey> {
    private String hearingTime;
    private LocalDate hearingDate;
    private LocalDateTime documentSubmittedDate;

    // Constructor, Getters, and Setters

    public SortKey(String hearingTime, LocalDate hearingDate, LocalDateTime documentSubmittedDate) {
        this.hearingTime = hearingTime;
        this.hearingDate = hearingDate;
        this.documentSubmittedDate = documentSubmittedDate;
    }

    // Comparable implementation
    @Override
    public int compareTo(SortKey other) {
        // Null checks with default ordering behavior
        if (other == null) {
            return 1;
        }
        int result = compareWithNullCheck(this.hearingDate, other.hearingDate);
        if (result != 0) {
            return result;
        }
        result = compareWithNullCheck(this.hearingTime, other.hearingTime);
        if (result != 0) {
            return result;
        }
        return compareWithNullCheck(this.documentSubmittedDate, other.documentSubmittedDate);
    }

    // Utility method for null-safe comparison
    private <T extends Comparable<T>> int compareWithNullCheck(T a, T b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }
}
