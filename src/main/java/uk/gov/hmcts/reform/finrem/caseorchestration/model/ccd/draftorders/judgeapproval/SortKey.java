package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Represents a sorting key for judge approval draft orders, incorporating hearing time, hearing date,
 * document submission date, and document filename. This class implements {@link Comparable} to enable
 * sorting based on the defined properties in a priority order.
 *
 * <p>Sorting Priority:</p>
 * <ol>
 *     <li>Hearing Date (ascending, nulls first)</li>
 *     <li>Hearing Time (ascending, nulls first)</li>
 *     <li>Document Submitted Date (ascending, nulls first)</li>
 *     <li>Document Filename (ascending, nulls first)</li>
 * </ol>
 *
 * <p>Null-safe comparison is applied for all fields.</p>
 *
 * <p>Usage Example:</p>
 * <pre>
 * SortKey key1 = new SortKey("10:00", LocalDate.of(2024, 11, 22), LocalDateTime.of(2024, 11, 21, 9, 0), "doc1.pdf");
 * SortKey key2 = new SortKey("11:00", LocalDate.of(2024, 11, 22), LocalDateTime.of(2024, 11, 21, 10, 0), "doc2.pdf");
 * int result = key1.compareTo(key2);
 * </pre>
 *
 * @see Comparable
 */
@Data
public class SortKey implements Comparable<SortKey> {
    private String hearingTime;
    private LocalDate hearingDate;
    private LocalDateTime documentSubmittedDate;
    private String documentFilename;

    /**
     * Constructs a {@link SortKey} with the specified details.
     *
     * @param hearingTime           the hearing time, can be null.
     * @param hearingDate           the hearing date, can be null.
     * @param documentSubmittedDate the document submission date and time, can be null.
     * @param documentFilename      the filename of the document, can be null.
     */
    public SortKey(String hearingTime, LocalDate hearingDate, LocalDateTime documentSubmittedDate, String documentFilename) {
        this.hearingTime = hearingTime;
        this.hearingDate = hearingDate;
        this.documentSubmittedDate = documentSubmittedDate;
        this.documentFilename = documentFilename;
    }

    /**
     * Compares this {@link SortKey} to another for sorting purposes.
     * The comparison order is determined by the priority of the fields as described above.
     *
     * @param other the other {@link SortKey} to compare against.
     * @return a negative integer, zero, or a positive integer as this object is less than,
     *              equal to, or greater than the specified object.
     */
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
        result = compareWithNullCheck(this.documentSubmittedDate, other.documentSubmittedDate);
        if (result != 0) {
            return result;
        }
        return compareWithNullCheck(this.documentFilename, other.documentFilename);
    }

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
