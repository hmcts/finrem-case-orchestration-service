package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.REJECTED_ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_PREVIEW_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

public class RefusalOrderDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private RefusalOrderDocumentService refusalOrderDocumentService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<CaseDetails> generateDocumentCaseDetailsCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateConsentOrderNotApproved()  {
        FinremCallbackRequest finremCallbackRequest =  buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        List<OrderRefusalOption> refusalOptionList = new ArrayList<>();
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT);
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD);
        OrderRefusalHolder orderRefusalCollectionNew = OrderRefusalHolder
            .builder()
            .orderRefusalJudgeName("Judge lastname")
            .orderRefusalDocs(caseDocument())
            .orderRefusalJudge(JudgeType.DISTRICT_JUDGE)
            .orderRefusalDate(LocalDate.now())
            .orderRefusalAddComments("This is test")
            .orderRefusalAfterText("Please begin here")
            .orderRefusal(refusalOptionList)
            .build();
        FinremCaseData finremCaseData = caseDetails.getData();
        finremCaseData.setOrderRefusalCollectionNew(orderRefusalCollectionNew);
        finremCaseData.getContactDetailsWrapper().setApplicantFmName("Poor");
        finremCaseData.getContactDetailsWrapper().setApplicantLname("Guy");
        finremCaseData.getContactDetailsWrapper().setAppRespondentFmName("john");
        finremCaseData.getContactDetailsWrapper().setAppRespondentLName("smith");
        finremCaseData.getNatureApplicationWrapper().setNatureOfApplication2(List.of(NatureApplication.LUMP_SUM_ORDER));

        FinremCaseData caseData = refusalOrderDocumentService.processConsentOrderNotApproved(caseDetails, AUTH_TOKEN);

        assertEquals(caseDocument(), caseData.getUploadOrder().get(0).getValue().getDocumentLink());
        assertEquals("generalOrder", caseData.getUploadOrder().get(0).getValue().getDocumentType().getValue());
        assertEquals("System Generated", caseData.getUploadOrder().get(0).getValue().getDocumentComment());
        assertCaseDataExtraFields();
        assertConsentedCaseDataExtraFields();
        assertCaseDocument(caseData.getUploadOrder().get(0).getValue().getDocumentLink());
    }

    @Test
    public void generateVariationOrderNotApproved() {
        FinremCallbackRequest finremCallbackRequest =  buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        List<OrderRefusalOption> refusalOptionList = new ArrayList<>();
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT);
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD);
        OrderRefusalHolder orderRefusalCollectionNew = OrderRefusalHolder
            .builder()
            .orderRefusalJudgeName("Judge lastname")
            .orderRefusalDocs(caseDocument())
            .orderRefusalJudge(JudgeType.DISTRICT_JUDGE)
            .orderRefusalDate(LocalDate.now())
            .orderRefusalAddComments("This is test")
            .orderRefusalAfterText("Please begin here")
            .orderRefusal(refusalOptionList)
            .build();
        FinremCaseData finremCaseData = caseDetails.getData();
        finremCaseData.setOrderRefusalCollectionNew(orderRefusalCollectionNew);
        finremCaseData.getContactDetailsWrapper().setApplicantFmName("Poor");
        finremCaseData.getContactDetailsWrapper().setApplicantLname("Guy");
        finremCaseData.getContactDetailsWrapper().setAppRespondentFmName("john");
        finremCaseData.getContactDetailsWrapper().setAppRespondentLName("smith");
        finremCaseData.getNatureApplicationWrapper().setNatureOfApplication2(List.of(NatureApplication.VARIATION_ORDER));

        FinremCaseData caseData = refusalOrderDocumentService.processConsentOrderNotApproved(caseDetails, AUTH_TOKEN);

        assertEquals(caseDocument(), caseData.getUploadOrder().get(0).getValue().getDocumentLink());
        assertEquals("generalOrder", caseData.getUploadOrder().get(0).getValue().getDocumentType().getValue());
        assertEquals("System Generated", caseData.getUploadOrder().get(0).getValue().getDocumentComment());
        assertCaseDataExtraFields();
        assertConsentedCaseDataExtraFields();
        assertCaseDocument(caseData.getUploadOrder().get(0).getValue().getDocumentLink());
    }

//    @Test
//    public void generateConsentOrderNotApprovedConsentInContested() throws Exception {
//        CaseDetails caseDetails = caseDetails("/fixtures/refusal-order-consent-in-contested.json");
//
//        Map<String, Object> caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
//
//        assertThat(getDocumentList(caseData, CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION), hasSize(1));
//        assertCaseDataExtraFields();
//        assertContestedCaseDataExtraFields("Birmingham Civil And Family Justice Centre");
//    }

//    @Test
//    public void multipleConsentOrderNotApprovedConsentInContested() throws Exception {
//        CaseDetails caseDetails = caseDetails("/fixtures/refusal-order-consent-in-contested.json");
//
//        Map<String, Object> caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
//
//        assertThat(getDocumentList(caseData, CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION), hasSize(1));
//        caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
//        assertThat(getDocumentList(caseData, CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION), hasSize(2));
//    }

//    @Test
//    public void multipleRefusalOrdersGenerateConsentOrderNotApproved() throws Exception {
//        CaseDetails caseDetails = caseDetails("/fixtures/model/copy-case-details-multiple-orders.json");
//
//        Map<String, Object> caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
//        List<OrderRefusalData> orderRefusalData = refusalOrderCollection(caseData);
//        assertThat(orderRefusalData.size(), is(2));
//        assertThat(orderRefusalData.get(0).getId(), Is.is("1"));
//        assertThat(orderRefusalData.get(1).getId(), Is.is("1"));
//
//        ConsentOrderData consentOrderData = consentOrderData(caseData);
//        assertThat(consentOrderData.getId(), is(notNullValue()));
//        assertThat(consentOrderData.getConsentOrder().getDocumentType(), is(REJECTED_ORDER_TYPE));
//        assertThat(consentOrderData.getConsentOrder().getDocumentDateAdded(), is(notNullValue()));
//        assertThat(consentOrderData.getConsentOrder().getDocumentComment(), is(equalTo("System Generated")));
//
//        assertCaseDocument(consentOrderData.getConsentOrder().getDocumentLink());
//    }

    @Test
    public void previewConsentOrderNotApproved() throws Exception {
        FinremCallbackRequest finremCallbackRequest =  buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        List<OrderRefusalOption> refusalOptionList = new ArrayList<>();
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT);
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD);
        OrderRefusalHolder orderRefusalCollectionNew = OrderRefusalHolder
            .builder()
            .orderRefusalJudgeName("Judge lastname")
            .orderRefusalDocs(caseDocument())
            .orderRefusalJudge(JudgeType.DISTRICT_JUDGE)
            .orderRefusalDate(LocalDate.now())
            .orderRefusalAddComments("This is test")
            .orderRefusalAfterText("Please begin here")
            .orderRefusal(refusalOptionList)
            .build();
        FinremCaseData finremCaseData = caseDetails.getData();
        finremCaseData.setOrderRefusalCollectionNew(orderRefusalCollectionNew);
        finremCaseData.getContactDetailsWrapper().setApplicantFmName("Poor");
        finremCaseData.getContactDetailsWrapper().setApplicantLname("Guy");
        finremCaseData.getContactDetailsWrapper().setAppRespondentFmName("john");
        finremCaseData.getContactDetailsWrapper().setAppRespondentLName("smith");
        finremCaseData.getNatureApplicationWrapper().setNatureOfApplication2(List.of(NatureApplication.VARIATION_ORDER));

        FinremCaseData caseData = refusalOrderDocumentService.previewConsentOrderNotApproved(AUTH_TOKEN, caseDetails);

        assertCaseDataExtraFields();
        assertConsentedCaseDataExtraFields();
        assertCaseDocument(caseData.getOrderRefusalPreviewDocument());
    }

    private List<CaseDocument> getDocumentList(Map<String, Object> data, String field) {
        return objectMapper.convertValue(data.get(field), new TypeReference<>() {
        });
    }

    private void assertCaseDataExtraFields() {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = generateDocumentCaseDetailsCaptor.getValue().getData();

        assertThat(caseData.get("ApplicantName"), is("Poor Guy"));
        assertThat(caseData.get("RespondentName"), is("john smith"));
        assertThat(caseData.get("RefusalOrderHeader"), is("Sitting in the Family Court"));
        List<String> list = objectMapper.convertValue(caseData.get("natureOfApplication2"), new TypeReference<>() {});
        assertNotNull(list);
        if (list.contains("Variation Order")) {
            assertThat(caseData.get("orderType"), is("variation"));
        } else {
            assertThat(caseData.get("orderType"), is("consent"));
        }
    }

    private void assertConsentedCaseDataExtraFields() {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = generateDocumentCaseDetailsCaptor.getValue().getData();

        assertThat(caseData.get("CourtName"), is("SITTING in private"));

        Map<String, Object> courtDetails = courtDetails(caseData.get("courtDetails"));
        assertThat(courtDetails.get("courtName"), is("Family Court at the Courts and Tribunal Service Centre"));
    }

    private Map<String, Object> courtDetails(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<>() {
        });
    }

    private void assertContestedCaseDataExtraFields(String expectedCourtDetailsCourtName) {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = generateDocumentCaseDetailsCaptor.getValue().getData();

        assertThat(caseData.get("CourtName"), is("SITTING AT the Family Court at the Birmingham Civil and Family Justice Centre"));

        Map<String, Object> courtDetails = courtDetails(caseData.get("courtDetails"));
        assertThat(courtDetails.get("courtName"), is(expectedCourtDetailsCourtName));

    }

    private CaseDocument getCaseDocument(Map<String, Object> caseData) {
        Object orderRefusalPreviewDocument = caseData.get(ORDER_REFUSAL_PREVIEW_COLLECTION);

        return objectMapper.convertValue(orderRefusalPreviewDocument, CaseDocument.class);
    }

    private ConsentOrderData consentOrderData(Map<String, Object> caseData) {
        List<ConsentOrderData> list = objectMapper.convertValue(caseData.get(UPLOAD_ORDER), new TypeReference<>() {
        });

        return list
            .stream()
            .filter(cd -> cd.getConsentOrder().getDocumentType().equals(REJECTED_ORDER_TYPE))
            .findFirst().orElseThrow(() -> new IllegalStateException(REJECTED_ORDER_TYPE + " missing"));
    }

    private CaseDetails caseDetails(String name) throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(name)) {
            return objectMapper.readValue(resourceAsStream, CaseDetails.class);
        }
    }

    private List<OrderRefusalData> refusalOrderCollection(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData.get(ORDER_REFUSAL_COLLECTION), new TypeReference<>() {
        });
    }


    private FinremCallbackRequest buildCallbackRequest(EventType eventType, CaseType caseType) {
        return FinremCallbackRequest
            .builder()
            .eventType(eventType)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(caseType)
                .data(new FinremCaseData()).state(State.APPLICATION_ISSUED).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(caseType)
                .data(new FinremCaseData()).state(State.APPLICATION_ISSUED).build())
            .build();
    }
}