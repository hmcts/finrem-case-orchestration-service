package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintCoverSheet;

import static org.junit.Assert.assertEquals;

public class BulkPrintCoverSheetTest {

    @Test
    public void checkAllStatusValues() {
        BulkPrintCoverSheet bulkPrintCoverSheet =
            BulkPrintCoverSheet.builder()
                .ccdNumber("ccdNumber")
                .recipientName("appRespondentFMName")
                .addressLine1("AddressLine1")
                .addressLine2("AddressLine2")
                .addressLine3("AddressLine3")
                .county("County")
                .postTown("PostTown")
                .postCode("PostCode")
                .build();
        assertEquals("ccdNumber", bulkPrintCoverSheet.getCcdNumber());
        assertEquals("appRespondentFMName", bulkPrintCoverSheet.getRecipientName());
        assertEquals("AddressLine1", bulkPrintCoverSheet.getAddressLine1());
        assertEquals("AddressLine2", bulkPrintCoverSheet.getAddressLine2());
        assertEquals("AddressLine3", bulkPrintCoverSheet.getAddressLine3());
        assertEquals("County", bulkPrintCoverSheet.getCounty());
        assertEquals("PostTown", bulkPrintCoverSheet.getPostTown());
        assertEquals("PostCode", bulkPrintCoverSheet.getPostCode());
    }
}
