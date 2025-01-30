package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a collection of orders to be sent within the case orchestration process.
 * This class encapsulates a list of {@link OrderToShareCollection} objects, which contain details
 * of the individual orders that can be shared.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdersToSend {

    /**
     * A list of order collections to be sent.
     */
    private List<OrderToShareCollection> value;
}
