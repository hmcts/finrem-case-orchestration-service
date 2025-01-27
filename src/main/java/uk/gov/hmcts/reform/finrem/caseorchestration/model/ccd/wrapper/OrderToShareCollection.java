package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection wrapper for an order to be shared within the case orchestration process.
 * This class encapsulates an instance of {@link OrderToShare}, providing a structured way
 * to handle the sharing of order documents.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderToShareCollection {

    /**
     * The order details encapsulated within this collection.
     */
    private OrderToShare value;
}
