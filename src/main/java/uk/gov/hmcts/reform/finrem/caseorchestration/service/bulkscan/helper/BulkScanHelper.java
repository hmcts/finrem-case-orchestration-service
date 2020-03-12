package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class BulkScanHelper {

    public static final Map<String, String> natureOfApplicationChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("Periodical Payment Order", "Periodical Payment Order")
            .put("Lump Sum Order", "Lump Sum Order")
            .put("Pension Sharing Order", "Pension Sharing Order")
            .put("Pension Attachment Order", "Pension Attachment Order")
            .put("Pension Compensation Sharing Order", "Pension Compensation Sharing Order")
            .put("Pension Compensation Attachment Order", "Pension Compensation Attachment Order")
            .put("A settlement or a transfer of property", "A settlement or a transfer of property")
            .put("Property Adjustment Order", "Property Adjustment Order")
            .build();

    public static final Map<String, String> dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("a lump sum order", "Lump Sum Order")
            .put("a property adjustment order", "Property Adjustment Order")
            .put("a pension sharing order", "Pension Sharing Order")
            .put("a pension compensation sharing order", "Pension Compensation Sharing Order")
            .build();
}
