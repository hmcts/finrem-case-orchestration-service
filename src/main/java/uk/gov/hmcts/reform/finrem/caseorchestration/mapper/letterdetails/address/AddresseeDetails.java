package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import java.util.Map;


@Data
@Builder
public class AddresseeDetails {
    private String addresseeName;
    private Map addressToSendTo;
    private String reference;

    private Address finremAddressToSendTo;
}
