package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RefusalOrderCollection;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class ContestedDraftOrderNotApprovedServiceTest extends BaseServiceTest {

    @Autowired private ContestedDraftOrderNotApprovedService refusalOrderService;
    @Autowired private DocumentConfiguration documentConfiguration;
    @Qualifier("finremCallbackRequestDeserializer")
    @Autowired private FinremCallbackRequestDeserializer deserializer;

    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean private ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    @Captor private ArgumentCaptor<Map<String, Object>> placeholdersMapArgumentCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any()))
            .thenReturn(newDocument());
    }

    @Test
    public void generateRefusalOrderWithOneReason() throws Exception {
        FinremCaseDetails caseDetails = contestedCaseDetails(false);
        when(contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(any(), any()))
            .thenReturn(getSingleReasonPlaceholdersMap());

        refusalOrderService.createAndSetRefusalOrderPreviewDocument(AUTH_TOKEN, caseDetails);

        Document result = caseDetails.getCaseData().getRefusalOrderPreviewDocument();
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsWithSingularReason();
    }

    @Test
    public void generateRefusalOrderWithMultipleReasons() throws Exception {
        FinremCaseDetails caseDetails = contestedCaseDetails(true);
        when(contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(any(), any()))
            .thenReturn(getMultiReasonPlaceholdersMap());
        refusalOrderService.createAndSetRefusalOrderPreviewDocument(AUTH_TOKEN, caseDetails);

        Document result = caseDetails.getCaseData().getRefusalOrderPreviewDocument();
        doCaseDocumentAssert(result);
        verifyAdditionalFieldsWithMultipleReasons();
    }

    @Test
    public void submitContestedGeneralOrder() throws Exception {
        FinremCaseDetails caseDetails = contestedCaseDetails(true);
        refusalOrderService.populateRefusalOrderCollection(caseDetails);

        List<RefusalOrderCollection> refusalOrders = caseDetails.getCaseData().getRefusalOrderCollection();

        assertThat(refusalOrders, hasSize(2));
        assertThat(refusalOrders.get(0).getValue().getRefusalOrderAdditionalDocument().getUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(refusalOrders.get(0).getValue().getRefusalOrderAdditionalDocument().getFilename(),
            is("app_docs.pdf"));
        assertThat(refusalOrders.get(0).getValue().getRefusalOrderAdditionalDocument().getBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(refusalOrders.get(1).getValue().getRefusalOrderAdditionalDocument().getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(refusalOrders.get(1).getValue().getRefusalOrderAdditionalDocument().getFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(refusalOrders.get(1).getValue().getRefusalOrderAdditionalDocument().getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));

        Document latestRefusalOrder = caseDetails.getCaseData().getLatestRefusalOrder();
        assertThat(latestRefusalOrder.getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestRefusalOrder.getFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(latestRefusalOrder.getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void getLatestRefusalReasonShouldReturnLatestReason() throws Exception {
        Optional<Document> doc = refusalOrderService.getLatestRefusalReason(contestedCaseDetails(true));
        assertThat(doc.isPresent(), is(true));
        assertThat(doc.get().getFilename(), is("app_docs.pdf"));
    }

    @Test
    public void getLatestRefusalReasonShouldReturnEmptyOptionalIfNoReason() {
        Optional<Document> doc = refusalOrderService.getLatestRefusalReason(FinremCaseDetails.builder().caseData(new FinremCaseData()).build());
        assertThat(doc.isPresent(), is(false));
    }

    private static void doCaseDocumentAssert(Document result) {
        assertThat(result.getFilename(), is(FILE_NAME));
        assertThat(result.getUrl(), is(DOC_URL));
        assertThat(result.getBinaryUrl(), is(BINARY_URL));
    }

    void verifyAdditionalFieldsWithMultipleReasons() {
        verifyDocumentServiceInteraction();

        Map<String, Object> data = getDataFromCaptor();
        assertCommonFields(data);
        assertThat(data.get("ContestOrderNotApprovedRefusalReasonsFormatted"),
            is("- Test Reason 1\n- Test Reason 2"));
    }

    void verifyAdditionalFieldsWithSingularReason() {
        verifyDocumentServiceInteraction();

        Map<String, Object> data = getDataFromCaptor();
        assertCommonFields(data);
        assertThat(data.get("ContestOrderNotApprovedRefusalReasonsFormatted"),
            is("- Draft order is not sufficient, signature is required"));
    }

    private Map<String, Object> getDataFromCaptor() {
        Map<String, Object> caseDetails = placeholdersMapArgumentCaptor.getValue();
        Map<String, Object> caseData = (Map<String, Object>) caseDetails.get(CASE_DETAILS);
        Map<String, Object> data = (Map<String, Object>) caseData.get(CASE_DATA);
        return data;
    }

    private void assertCommonFields(Map<String, Object> data) {
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("Court"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("JudgeDetails"), is("Her Honour Judge Contested"));
    }

    private void verifyDocumentServiceInteraction() {
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedDraftOrderNotApprovedTemplate()),
            eq(documentConfiguration.getContestedDraftOrderNotApprovedFileName()));
    }

    private FinremCaseDetails contestedCaseDetails(boolean multipleReasons) throws Exception {
        if (multipleReasons) {
            return deserializer.deserialize(getResource("/fixtures/refusal-order-contested.json"))
                .getCaseDetails();
        } else {
            return deserializer.deserialize(getResource("/fixtures/refusal-order-singular-contested.json"))
                .getCaseDetails();
        }
    }

    private Map<String, Object> getMultiReasonPlaceholdersMap() {
        Map<String, Object> placeholders = Map.of(
            "ApplicantName", "Contested Applicant Name",
            "RespondentName", "Contested Respondent Name",
            "Court", "Nottingham County Court and Family Court",
            "JudgeDetails", "Her Honour Judge Contested",
            "ContestOrderNotApprovedRefusalReasonsFormatted", "- Test Reason 1\n- Test Reason 2"
            );

        Map<String, Object> caseDataMap = Map.of(
            CASE_DATA, placeholders,
            "id", 123L);
        return Map.of(CASE_DETAILS, caseDataMap);
    }

    private Map<String, Object> getSingleReasonPlaceholdersMap() {
        Map<String, Object> placeholders = Map.of(
            "ApplicantName", "Contested Applicant Name",
            "RespondentName", "Contested Respondent Name",
            "Court", "Nottingham County Court and Family Court",
            "JudgeDetails", "Her Honour Judge Contested",
            "ContestOrderNotApprovedRefusalReasonsFormatted", "- Draft order is not sufficient, signature is required"
        );

        Map<String, Object> caseDataMap = Map.of(
            CASE_DATA, placeholders,
            "id", 123L);
        return Map.of(CASE_DETAILS, caseDataMap);
    }
}
