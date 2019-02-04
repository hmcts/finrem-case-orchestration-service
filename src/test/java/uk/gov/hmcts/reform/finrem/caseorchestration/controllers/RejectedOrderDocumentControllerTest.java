package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BIN_DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.REJECTED_ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;

@RunWith(SpringRunner.class)
@WebMvcTest(RejectedOrderDocumentController.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public class RejectedOrderDocumentControllerTest {

    private static final String API_URL = "/case-orchestration/documents/consent-order-not-approved";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private DocumentService documentService;

    private MockMvc mvc;
    private JsonNode requestContent;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));
    }

    @Test
    public void generateConsentOrderNotApprovedSuccess() throws Exception {
        when(documentService.generateConsentOrderNotApproved(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenReturn(consentOrderData());

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadOrder[0].id", is(notNullValue())))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentType", is(REJECTED_ORDER_TYPE)))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentDateAdded", is(notNullValue())))
                .andExpect(jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_url", is(DOC_URL)))
                .andExpect(
                        jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_filename", is(DOC_NAME)))
                .andExpect(
                        jsonPath("$.data.uploadOrder[0].value.DocumentLink.document_binary_url",
                                is(BIN_DOC_URL)))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    private ConsentOrderData consentOrderData() {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(REJECTED_ORDER_TYPE);
        consentOrder.setDocumentLink(caseDocument());
        consentOrder.setDocumentDateAdded(new Date());

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(UUID.randomUUID().toString());
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    @Test
    public void generateConsentOrderNotApproved400() throws Exception {
        mvc.perform(post(API_URL)
                .content("kwuilebge")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateConsentOrderNotApproved500() throws Exception {
        when(documentService.generateConsentOrderNotApproved(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}