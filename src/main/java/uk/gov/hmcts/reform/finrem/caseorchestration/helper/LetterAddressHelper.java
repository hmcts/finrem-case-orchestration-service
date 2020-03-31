package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.LINE_SEPARATOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Component
public class LetterAddressHelper {

    private List<String> addressLines = new ArrayList<>();

    public String formatAddressForLetterPrinting(Map<String, Object> address) {

        addressLines.add(String.valueOf((address.get("AddressLine1"))));
        addressLines.add(String.valueOf((address.get("AddressLine2"))));
        addressLines.add(String.valueOf((address.get("AddressLine3"))));
        addressLines.add(String.valueOf((address.get("County"))));
        addressLines.add(String.valueOf((address.get("Country"))));
        addressLines.add(String.valueOf((address.get("PostTown"))));
        addressLines.add(String.valueOf((address.get("PostCode"))));

        addressLines.removeAll(Arrays.asList("", null, "null"));

        return String.join(LINE_SEPARATOR, addressLines);
    }
}