package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
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

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Autowired
    private ContestedDraftOrderNotApprovedService refusalOrderService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentConfiguration documentConfiguration;

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

        assertThat(refusalOrders, hasSize(2));
        assertThat(refusalOrders.get(0).getId(), is("1234"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentFilename(),
            is("app_docs.pdf"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(refusalOrders.get(1).getId(), notNullValue());
        assertThat(refusalOrders.get(1).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(refusalOrders.get(1).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(refusalOrders.get(1).getContestedRefusalOrder().getRefusalOrderAdditionalDocument().getDocumentBinaryUrl(),
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
    public void getLatestRefusalReasonShouldReturnLatestReason() throws Exception {
        Optional<CaseDocument> doc = refusalOrderService.getLatestRefusalReason(contestedCaseDetails(true));
        assertThat(doc.isPresent(), is(true));
        assertThat(doc.get().getDocumentFilename(), is("app_docs.pdf"));
    }

    @Test
    public void getLatestRefusalReasonShouldReturnEmptyOptionalIfNoReason() {
        Optional<CaseDocument> doc = refusalOrderService.getLatestRefusalReason(CaseDetails.builder().data(new HashMap<String, Object>()).build());
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
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq(documentConfiguration.getContestedDraftOrderNotApprovedTemplate()),
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
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq(documentConfiguration.getContestedDraftOrderNotApprovedTemplate()),
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