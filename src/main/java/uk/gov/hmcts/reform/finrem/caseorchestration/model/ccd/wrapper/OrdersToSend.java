package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

/**
 * Represents a collection of orders to be sent within the case orchestration process.
 * This class encapsulates a list of {@link OrderToShareCollection} objects, which contain details
 * of the individual orders that can be shared.
 */
@ComplexType(name = "FR_ordersToSend", generate = true)
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
    @CCD(
            label = "Which order(s) would you like to send?",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_orderToShare"
    )
    private List<OrderToShareCollection> value;
}
