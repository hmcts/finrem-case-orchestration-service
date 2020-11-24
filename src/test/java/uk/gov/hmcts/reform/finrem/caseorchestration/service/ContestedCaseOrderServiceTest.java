package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

public class ContestedCaseOrderServiceTest extends BaseServiceTest {

    @Autowired
    private ContestedCaseOrderService contestedCaseOrderService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @Test
    public void givenNoGeneralOrderPresent_whenPrintAndMailGeneralOrderTriggered_thenDocumentsAreNotPrinted() {
        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        caseDetails.getData().remove(GENERAL_ORDER_LATEST_DOCUMENT);

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void whenPrintAndMailGeneralOrderTriggered_thenBothApplicantAndRespondentPacksArePrinted() {
        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(true);

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenShouldNotPrintPackForApplicant_whenPrintAndMailGeneralOrderTriggered_thenOnlyRespondentPacksIsPrinted() {
        CaseDetails caseDetails = generalOrderContestedCaseDetails();
        when(bulkPrintService.shouldPrintForApplicant(any())).thenReturn(false);

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());
    }

    private CaseDetails generalOrderContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenSendToBulkPrintWhenPaperCase() {
        CaseDetails caseDetails = hearingDocumentsContestedCaseDetails();

        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        //verify(bulkPrintService, times(1)).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenAllHearingDocumentsArePresentThenDoNotSendToBulkPrintWhenDigitalCase() {
        CaseDetails caseDetails = hearingDocumentsContestedCaseDetails();
        caseDetails.getData().remove(PAPER_APPLICATION);

        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(0)).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, times(0)).printRespondentDocuments(any(), any(), any());
    }

    private CaseDetails hearingDocumentsContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-contested-hearing.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
