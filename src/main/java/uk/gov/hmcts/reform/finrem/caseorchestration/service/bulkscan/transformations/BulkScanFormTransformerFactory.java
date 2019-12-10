package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.exception.bulk.scan.UnsupportedFormTypeException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@Component
public class BulkScanFormTransformerFactory {

    @Autowired
    private FormAToCaseTransformer formAToCaseTransformer;

    private static Map<String, BulkScanFormTransformer> bulkScanFormTransformerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        bulkScanFormTransformerMap.put(FORM_A, formAToCaseTransformer);
    }

    public BulkScanFormTransformer getTransformer(String formType) {
        if (!bulkScanFormTransformerMap.containsKey(formType)) {
            throw new UnsupportedFormTypeException(format("Form type \"%s\" is not supported.", formType));
        }

        return bulkScanFormTransformerMap.get(formType);
    }
}