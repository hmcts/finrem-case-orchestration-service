package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.MoveCollectionService;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(HearingOrderController.class)
public class HearingOrderControllerTest extends BaseControllerTest {
    private static final String CONVERSION_URL = "/case-orchestration/hearing-order/store";

    @MockBean
    private DocumentHelper documentHelper;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @MockBean
    private MoveCollectionService moveCollectionService;

    @Before
    public void setUp()  {
        super.setUp();
        MockMvcBuilders.webAppContextSetup(applicationContext).build();
        try {
            requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/hearing-order-conversion.json").toURI()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void convertPdfDocument() throws Exception {
        when(genericDocumentService.stampDocument(any(), anyString())).thenReturn(getCaseDocument());
        when(moveCollectionService.moveCollection(anyMap(), anyString(), anyString()))
            .thenReturn(new HashMap<String, Object>() {});
        when(documentHelper.getLatestContestedDraftOrderCollection(anyMap())).thenReturn(getCaseDocument());

        mvc.perform(post(CONVERSION_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void throwsExceptionIfNoDocumentFound() throws Exception {
        when(genericDocumentService.stampDocument(any(), anyString())).thenReturn(getCaseDocument());
        when(moveCollectionService.moveCollection(anyMap(), anyString(), anyString()))
            .thenReturn(new HashMap<String, Object>() {});
        when(documentHelper.getLatestContestedDraftOrderCollection(anyMap())).thenReturn(null);

        mvc.perform(post(CONVERSION_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is4xxClientError());
    }

}
