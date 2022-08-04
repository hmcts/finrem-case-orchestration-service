package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderDocumentType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.REJECTED_ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class RefusalOrderDocumentServiceTest extends BaseServiceTest {

    @Autowired private RefusalOrderDocumentService refusalOrderDocumentService;

    @MockBean private GenericDocumentService genericDocumentService;

    @Captor private ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;


    @Before
    public void setUp() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void generateConsentOrderNotApproved() throws Exception {
        FinremCaseDetails caseDetails =
            finremCaseDetailsFromResource(getResource("/fixtures/model/case-details-as-callback-request.json"), mapper);

        FinremCaseData caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        UploadOrderCollection consentOrderData = consentOrderData(caseData);

        assertThat(consentOrderData.getValue(), is(notNullValue()));
        assertThat(consentOrderData.getValue().getDocumentType(), is(UploadOrderDocumentType.GENERAL_ORDER));
        assertThat(consentOrderData.getValue().getDocumentDateAdded(), is(notNullValue()));
        assertThat(consentOrderData.getValue().getDocumentComment(), is(equalTo("System Generated")));

        assertCaseDataExtraFields(caseData);
        assertConsentedCaseDataExtraFields();
        assertCaseDocument(consentOrderData.getValue().getDocumentLink());
    }

    @Test
    public void generateConsentOrderNotApprovedConsentInContested() throws Exception {
        FinremCaseDetails caseDetails =
            finremCaseDetailsFromResource(getResource("/fixtures/refusal-order-consent-in-contested-as-callback.json"), mapper);

        FinremCaseData caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        assertThat(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders(), hasSize(1));

        assertCaseDataExtraFields(caseData);
        assertContestedCaseDataExtraFields("Birmingham Civil And Family Justice Centre");
    }

    @Test
    public void multipleConsentOrderNotApprovedConsentInContested() throws Exception {
        FinremCaseDetails caseDetails =
            finremCaseDetailsFromResource(getResource("/fixtures/refusal-order-consent-in-contested-as-callback.json"), mapper);

        FinremCaseData caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);

        assertThat(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders(), hasSize(1));
        caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        assertThat(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders(), hasSize(2));
    }

    @Test
    public void multipleRefusalOrdersGenerateConsentOrderNotApproved() throws Exception {
        FinremCaseDetails caseDetails =
            finremCaseDetailsFromResource(getResource("/fixtures/model/copy-case-details-multiple-orders-callback.json"), mapper);

        FinremCaseData caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        List<OrderRefusalCollection> orderRefusalData = caseData.getOrderRefusalCollection();
        assertThat(orderRefusalData.size(), is(2));

        UploadOrderCollection consentOrderData = consentOrderData(caseData);
        assertThat(consentOrderData.getValue().getDocumentType(), is(UploadOrderDocumentType.GENERAL_ORDER));
        assertThat(consentOrderData.getValue().getDocumentDateAdded(), is(notNullValue()));
        assertThat(consentOrderData.getValue().getDocumentComment(), is(equalTo("System Generated")));

        assertCaseDocument(consentOrderData.getValue().getDocumentLink());
    }

    @Test
    public void previewConsentOrderNotApproved() throws Exception {
        FinremCaseDetails caseDetails =
            finremCaseDetailsFromResource(getResource("/fixtures/model/case-details-as-callback-request.json"), mapper);
        FinremCaseData caseData = refusalOrderDocumentService.previewConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        Document caseDocument = caseData.getOrderRefusalPreviewDocument();

        assertCaseDataExtraFields(caseData);
        assertConsentedCaseDataExtraFields();
        assertCaseDocument(caseDocument);
    }

    private void assertCaseDataExtraFields(FinremCaseData finremCaseData) {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            placeholdersMapCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = getDataFromCaptor(placeholdersMapCaptor);

        assertThat(caseData.get("ApplicantName"), is("Poor Guy"));
        assertThat(caseData.get("RespondentName"), is("john smith"));
        assertThat(caseData.get("RefusalOrderHeader"), is("Sitting in the Family Court"));
        List<NatureApplication> natureOfApplication2List = finremCaseData.getNatureApplicationWrapper().getNatureOfApplication2();

        if (!CollectionUtils.isEmpty(natureOfApplication2List) && natureOfApplication2List.contains(NatureApplication.VARIATION_ORDER)) {
            assertThat(caseData.get("orderType"), is("variation"));
        } else {
            assertThat(caseData.get("orderType"), is("consent"));
        }

    }

    private void assertConsentedCaseDataExtraFields() {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(), placeholdersMapCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = getDataFromCaptor(placeholdersMapCaptor);

        assertThat(caseData.get("CourtName"), is("SITTING in private"));

        Map<String, Object> courtDetails = (Map<String, Object>) caseData.get("courtDetails");
        assertThat(courtDetails.get("courtName"), is("Family Court at the Courts and Tribunal Service Centre"));
    }

    private void assertContestedCaseDataExtraFields(String expectedCourtDetailsCourtName) {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(), placeholdersMapCaptor.capture(),
            any(), any());
        Map<String, Object> caseData = getDataFromCaptor(placeholdersMapCaptor);

        assertThat(caseData.get("CourtName"), is("SITTING AT the Family Court at the Birmingham Civil And Family Justice Centre"));

        Map<String, Object> courtDetails = (Map<String, Object>) caseData.get("courtDetails");
        assertThat(courtDetails.get("courtName"), is(expectedCourtDetailsCourtName));

    }

    private UploadOrderCollection consentOrderData(FinremCaseData caseData) {
        List<UploadOrderCollection> list = caseData.getUploadOrder();

        return list.stream()
            .filter(cd -> cd.getValue().getDocumentType().equals(UploadOrderDocumentType.GENERAL_ORDER))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(REJECTED_ORDER_TYPE + " missing"));
    }
}