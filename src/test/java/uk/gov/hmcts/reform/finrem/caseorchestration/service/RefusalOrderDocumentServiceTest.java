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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

public class RefusalOrderDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private RefusalOrderDocumentService refusalOrderDocumentService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @MockBean
    private IdamService idamService;

    @Captor
    private ArgumentCaptor<CaseDetails> generateDocumentCaseDetailsCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateConsentOrderNotApproved() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
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
        finremCaseData.setOrderRefusalOnScreen(orderRefusalCollectionNew);
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
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
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
        finremCaseData.setOrderRefusalOnScreen(orderRefusalCollectionNew);
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

    @Test
    public void generateConsentOrderNotApprovedConsentInContested() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.CONSENT_ORDER_NOT_APPROVED, CaseType.CONTESTED);
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

        finremCaseData.setOrderRefusalOnScreen(orderRefusalCollectionNew);
        finremCaseData.getContactDetailsWrapper().setApplicantFmName("Poor");
        finremCaseData.getContactDetailsWrapper().setApplicantLname("Guy");
        finremCaseData.getContactDetailsWrapper().setRespondentFmName("john");
        finremCaseData.getContactDetailsWrapper().setRespondentLname("smith");
        finremCaseData.getNatureApplicationWrapper().setNatureOfApplication2(List.of(NatureApplication.LUMP_SUM_ORDER));
        DefaultCourtListWrapper listWrapper = DefaultCourtListWrapper
            .builder()
            .birminghamCourtList(BirminghamCourt.BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE)
            .build();
        AllocatedRegionWrapper wrapper = AllocatedRegionWrapper
            .builder()
            .regionList(Region.MIDLANDS)
            .midlandsFrcList(RegionMidlandsFrc.BIRMINGHAM)
            .courtListWrapper(listWrapper)
            .build();
        finremCaseData.getRegionWrapper().setAllocatedRegionWrapper(wrapper);
        caseDetails.getData().getConsentOrderWrapper().setConsentD81Question(YesOrNo.YES);
        FinremCaseData caseData = refusalOrderDocumentService.processConsentOrderNotApproved(caseDetails, AUTH_TOKEN);

        assertEquals(caseDocument(), caseData.getUploadOrder().get(0).getValue().getDocumentLink());
        assertEquals("generalOrder", caseData.getUploadOrder().get(0).getValue().getDocumentType().getValue());
        assertEquals("System Generated", caseData.getUploadOrder().get(0).getValue().getDocumentComment());
        assertCaseDataExtraFields();
        assertContestedCaseDataExtraFields();
        assertCaseDocument(caseData.getUploadOrder().get(0).getValue().getDocumentLink());
        List<ConsentOrderCollection> consentedNotApprovedOrders = caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders();
        assertCaseDocument(consentedNotApprovedOrders.get(0).getApprovedOrder().getConsentOrder());
    }

    @Test
    public void previewConsentOrderNotApproved() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
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
        finremCaseData.setOrderRefusalOnScreen(orderRefusalCollectionNew);
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


    @Test
    public void setDefaults() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.REJECT_ORDER, CaseType.CONSENTED);
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();

        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("moj moj");

        FinremCaseData caseData = refusalOrderDocumentService.setDefaults(finremCaseData, AUTH_TOKEN);
        OrderRefusalHolder orderRefusalCollectionNew = caseData.getOrderRefusalOnScreen();
        assertEquals("moj moj", orderRefusalCollectionNew.getOrderRefusalJudgeName());
        assertEquals(LocalDate.now(), orderRefusalCollectionNew.getOrderRefusalDate());

    }


    private void assertCaseDataExtraFields() {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = generateDocumentCaseDetailsCaptor.getValue().getData();

        assertThat(caseData.get("ApplicantName"), is("Poor Guy"));
        assertThat(caseData.get("RespondentName"), is("john smith"));
        assertThat(caseData.get("RefusalOrderHeader"), is("Sitting in the Family Court"));
        List<String> list = objectMapper.convertValue(caseData.get("natureOfApplication2"), new TypeReference<>() {
        });
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

    private void assertContestedCaseDataExtraFields() {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = generateDocumentCaseDetailsCaptor.getValue().getData();

        assertThat(caseData.get("CourtName"), is("SITTING AT the Family Court at the Birmingham Civil and Family Justice Centre"));

        Map<String, Object> courtDetails = courtDetails(caseData.get("courtDetails"));
        assertThat(courtDetails.get("courtName"), is("Birmingham Civil And Family Justice Centre"));

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