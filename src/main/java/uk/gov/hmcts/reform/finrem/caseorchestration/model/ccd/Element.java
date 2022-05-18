package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Element<T> {
    private UUID id;

    @NotNull
    @Valid
    private T value;

    public static <T> Element<T> newElement(T value) {
        return Element.<T>builder().value(value).build();
    }

    public static <T> Element<T> element(UUID id, T element) {
        return Element.<T>builder()
            .id(id)
            .value(element)
            .build();
    }
}
