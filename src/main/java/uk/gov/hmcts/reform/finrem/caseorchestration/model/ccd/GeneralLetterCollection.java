package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralLetterCollection implements CaseDocumentsDiscovery {
    private GeneralLetter value;

    @Override
    public List<CaseDocument> discover() {
        return Stream.of(
                ofNullable(value)
                    .map(GeneralLetter::getGeneratedLetter)
                    .map(List::of)
                    .orElse(List.of()).stream(),
                ofNullable(value)
                    .map(GeneralLetter::getGeneralLetterUploadedDocument)
                    .map(List::of)
                    .orElse(List.of()).stream(),
                ofNullable(ofNullable(value).orElse(GeneralLetter.builder().build())
                    .getGeneralLetterUploadedDocuments())
                    .orElse(List.of())
                    .stream()
                    .flatMap(d -> d.discover().stream())
            )
            .flatMap(s -> s)
            .filter(Objects::nonNull)
            .toList();
    }
}
