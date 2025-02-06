package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedRefusalOrderData;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;

public class ContestedDraftOrderNotApprovedServiceTest extends BaseServiceTest {

    @Autowired
    private ContestedDraftOrderNotApprovedService refusalOrderService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentConfiguration documentConfiguration;

    @MockitoBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateRefusalOrderWithOneReason() throws Exception {
        Map<String, Object> documentMap = refusalOrderService.createRefusalOrder(AUTH_TOKEN, contestedCaseDetails(false));

        CaseDocument result = (CaseDocument) documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsWithSingularReason();
    }

    @Test
    public void generateRefusalOrderWithMultipleReasons() throws Exception {
        Map<String, Object> documentMap = refusalOrderService.createRefusalOrder(AUTH_TOKEN, contestedCaseDetails(true));

        CaseDocument result = (CaseDocument) documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);
        verifyAdditionalFieldsWithMultipleReasons();
    }

    @Test
    public void submitContestedGeneralOrder() throws Exception {
        Map<String, Object> documentMap = refusalOrderService.populateRefusalOrderCollection(contestedCaseDetails(true));

        List<ContestedRefusalOrderData> refusalOrders =
            (List<ContestedRefusalOrderData>) documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION);

        assertThat(refusalOrders, hasSize(1));

        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));


        CaseDocument latestRefusalOrder = (CaseDocument) documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT);
        assertThat(latestRefusalOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestRefusalOrder.getDocumentFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(latestRefusalOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void getLatestRefusalReasonShouldReturnEmptyOptionalIfNoReason() {
        Optional<CaseDocument> doc = refusalOrderService.getLatestRefusalReason(CaseDetails.builder().data(new HashMap<>()).build());
        assertThat(doc.isPresent(), is(false));
    }

    private CaseDetails contestedCaseDetails(boolean multipleReasons) throws Exception {
        if (multipleReasons) {
            try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/refusal-order-contested.json")) {
                return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            }
        } else {
            try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/refusal-order-singular-contested.json")) {
                return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            }
        }
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }

    void verifyAdditionalFieldsWithMultipleReasons() {
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedDraftOrderNotApprovedTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getContestedDraftOrderNotApprovedFileName()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("Court"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("JudgeDetails"), is("Her Honour Judge Contested"));
        assertThat(data.get("ContestOrderNotApprovedRefusalReasonsFormatted"),
            is("- Test Reason 1\n- Test Reason 2"));
    }

    void verifyAdditionalFieldsWithSingularReason() {
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedDraftOrderNotApprovedTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getContestedDraftOrderNotApprovedFileName()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("Court"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("JudgeDetails"), is("Her Honour Judge Contested"));
        assertThat(data.get("ContestOrderNotApprovedRefusalReasonsFormatted"),
            is("- Draft order is not sufficient, signature is required"));
    }
}
