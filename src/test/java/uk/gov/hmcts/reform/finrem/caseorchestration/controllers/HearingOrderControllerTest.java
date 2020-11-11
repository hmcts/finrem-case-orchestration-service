package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(HearingOrderController.class)
public class HearingOrderControllerTest extends BaseControllerTest {
    private static final String CONVERSION_URL = "/case-orchestration/hearing-order/store";

    @MockBean
    private DocumentHelper documentHelper;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @InjectMocks
    private HearingOrderController hearingOrderController;

    @Before
    public void setUp()  {
        super.setUp();
        MockMvcBuilders.webAppContextSetup(applicationContext).build();
        hearingOrderController = new HearingOrderController(documentHelper, genericDocumentService);
    }

    @Test
    public void convertPdfDocument() throws Exception {
        when(genericDocumentService.stampDocument(any(), anyString())).thenReturn(getCaseDocument());
        when(documentHelper.moveCollection(anyMap(), anyString(), anyString()))
            .thenReturn(new HashMap<String, Object>() {});
        when(documentHelper.getLatestContestedDraftOrderCollection(anyMap())).thenReturn(getCaseDocument());
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.storeHearingOrder("authtoken", buildCallbackRequest());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        CaseDocument responseDocument = (CaseDocument) response.getBody().getData().get("latestDraftHearingOrder");
        assertThat(responseDocument.getDocumentFilename(), is("doc1"));
        assertThat(responseDocument.getDocumentBinaryUrl(), is("http://doc1/binary"));
    }

    @Test(expected = InvalidCaseDataException.class)
    public void throwsExceptionIfNoDocumentFound() throws Exception {
        when(genericDocumentService.stampDocument(any(), anyString())).thenReturn(getCaseDocument());
        when(documentHelper.moveCollection(anyMap(), anyString(), anyString()))
            .thenReturn(new HashMap<String, Object>() {});
        when(documentHelper.getLatestContestedDraftOrderCollection(anyMap())).thenReturn(null);
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response = hearingOrderController.storeHearingOrder("authtoken", buildCallbackRequest());
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

}
