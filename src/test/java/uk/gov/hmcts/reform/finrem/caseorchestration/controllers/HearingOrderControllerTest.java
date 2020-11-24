package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(HearingOrderController.class)
public class HearingOrderControllerTest extends BaseControllerTest {

    @Autowired HearingOrderController hearingOrderController;

    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private CaseDataService caseDataService;
    @MockBean private DocumentHelper documentHelper;

    @Test
    public void convertPdfDocument() {
        when(genericDocumentService.stampDocument(any(), anyString())).thenReturn(getCaseDocument());
        when(documentHelper.getLatestContestedDraftOrderCollection(anyMap())).thenReturn(getCaseDocument());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.storeHearingOrder("authtoken", buildCallbackRequest());

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        CaseDocument responseDocument = (CaseDocument) response.getBody().getData().get("latestDraftHearingOrder");
        assertThat(responseDocument.getDocumentFilename(), is("doc1"));
        assertThat(responseDocument.getDocumentBinaryUrl(), is("http://doc1/binary"));
    }

    @Test(expected = InvalidCaseDataException.class)
    public void throwsExceptionIfNoDocumentFound() {
        when(genericDocumentService.stampDocument(any(), anyString())).thenReturn(getCaseDocument());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.storeHearingOrder("authtoken", buildCallbackRequest());

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
