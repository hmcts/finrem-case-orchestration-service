package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/**
 * Represents a collection containing a single UUID value.
 * This class is used to encapsulate a UUID in a structured format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UuidCollection {
    private UUID value;
}
