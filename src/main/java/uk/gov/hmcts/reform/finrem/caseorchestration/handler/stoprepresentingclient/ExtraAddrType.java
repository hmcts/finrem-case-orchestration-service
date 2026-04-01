package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ExtraAddrType {

    APPLICANT("applicant", "Applicant"),
    RESPONDENT("respondent", "Respondent"),
    INTERVENER1("intervener1", "Intervener 1"),
    INTERVENER2("intervener2", "Intervener 2"),
    INTERVENER3("intervener3", "Intervener 3"),
    INTERVENER4("intervener4", "Intervener 4");

    private final String id;

    private final String description;

    /**
     * Returns the human-readable description for the given address type id.
     *
     * <p>The lookup is case-insensitive and matches against the enum {@code id} field.
     *
     * @param id the identifier of the {@link ExtraAddrType}; may be {@code null}
     * @return an {@link Optional} containing the corresponding description if a match is found,
     *         otherwise {@link Optional#empty()}
     */
    public static Optional<String> describe(String id) {
        return Arrays.stream(values())
            .filter(e -> e.id.equalsIgnoreCase(id))
            .map(e -> e.description)
            .findFirst();
    }
}
