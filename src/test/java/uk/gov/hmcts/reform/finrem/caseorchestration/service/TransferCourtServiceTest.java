package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TransferLocalCourtEmail;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRANSFER_COURTS_COLLECTION;

public class TransferCourtServiceTest extends BaseServiceTest {

    @Autowired
    private TransferCourtService transferCourtService;

    @Test
    public void generateTransferCourtEmail() throws Exception {
        CaseDetails caseDetails = caseDetailsConsented();
        transferCourtService.storeTransferToCourtEmail(caseDetails);
        List<TransferLocalCourtEmail> transferCourtEmailList = (List<TransferLocalCourtEmail>) caseDetails.getData().get(TRANSFER_COURTS_COLLECTION);
        assertThat(transferCourtEmailList, hasSize(2));

        TransferLocalCourtEmail originalEmail = transferCourtEmailList.get(0);
        assertThat(originalEmail.getId(), notNullValue());
        assertThat(originalEmail.getTransferLocalCourtEmailData().getCourtEmail(), is("ExistingTestCourt@test.com"));
        assertThat(originalEmail.getTransferLocalCourtEmailData().getCourtName(), is("Existing Test Court Name"));
        assertThat(originalEmail.getTransferLocalCourtEmailData().getCourtInstructions(), is("Existing Test Email Instructions"));

        TransferLocalCourtEmail addedEmail = transferCourtEmailList.get(1);
        assertThat(addedEmail.getId(), notNullValue());
        assertThat(addedEmail.getTransferLocalCourtEmailData().getCourtEmail(), is("NewTestCourt@test.com"));
        assertThat(addedEmail.getTransferLocalCourtEmailData().getCourtName(), is("New Test Court Name"));
        assertThat(addedEmail.getTransferLocalCourtEmailData().getCourtInstructions(), is("New Test Email Instructions"));
    }

    private CaseDetails caseDetailsConsented() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/transfer-to-local-court-email-consented.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }
}