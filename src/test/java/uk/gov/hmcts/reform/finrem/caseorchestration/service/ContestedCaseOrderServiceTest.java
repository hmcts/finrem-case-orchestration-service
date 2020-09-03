package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

public class ContestedCaseOrderServiceTest extends BaseServiceTest {

    @Autowired
    private ContestedCaseOrderService contestedCaseOrderService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @Test
    public void givenNoGeneralOrderPresent_whenPrintAndMailGeneralOrderTriggered_thenErrorIsReturned() {
        CaseDetails caseDetails = contestedCaseDetails();
        caseDetails.getData().remove(GENERAL_ORDER_LATEST_DOCUMENT);

        List<String> errors = contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        assertThat(errors, hasSize(1));
    }

    @Test
    public void whenPrintAndMailGeneralOrderTriggered_thenBothApplicantAndRespondentPacksArePrinted() {
        CaseDetails caseDetails = contestedCaseDetails();

        List<String> errors = contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        assertThat(errors, is(empty()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());
    }

    private CaseDetails contestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
