package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.LINE_SEPARATOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Component
public class LetterAddressHelper {

    private List<String> addressLines = new ArrayList<>();

    public String formatAddressForLetterPrinting(Map<String, Object> address) {

        addressLines.add(nullToEmpty(address.get("AddressLine1")));
        addressLines.add(nullToEmpty(address.get("AddressLine2")));
        addressLines.add(nullToEmpty(address.get("AddressLine3")));
        addressLines.add(nullToEmpty(address.get("County")));
        addressLines.add(nullToEmpty(address.get("Country")));
        addressLines.add(nullToEmpty(address.get("PostTown")));
        addressLines.add(nullToEmpty(address.get("PostCode")));

        return String.join(LINE_SEPARATOR, addressLines);
    }
}
