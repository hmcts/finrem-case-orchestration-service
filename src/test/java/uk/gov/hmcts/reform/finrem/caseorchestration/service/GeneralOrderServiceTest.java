package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrderAddressTo;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrderCollection;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class GeneralOrderServiceTest extends BaseServiceTest {

    @Autowired private GeneralOrderService generalOrderService;
    @Autowired private DocumentConfiguration documentConfiguration;
    @Autowired private FinremCallbackRequestDeserializer deserializer;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void generateGeneralOrderConsented() throws Exception {
        FinremCaseDetails caseDetails = consentedCaseDetails();
        generalOrderService.createAndSetGeneralOrder(AUTH_TOKEN, caseDetails);

        Document result = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderPreviewDocument();
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsConsented();
    }

    @Test
    public void generateGeneralOrderContested() throws Exception {
        FinremCaseDetails caseDetails = contestedCaseDetails();
        generalOrderService.createAndSetGeneralOrder(AUTH_TOKEN, caseDetails);

        Document result = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderPreviewDocument();
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsContested();
    }

    @Test
    public void submitContestedGeneralOrder() throws Exception {
        FinremCaseDetails caseDetails = contestedCaseDetails();
        generalOrderService.populateGeneralOrderCollection(caseDetails);
        List<ContestedGeneralOrderCollection> generalOrders = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrders();

        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getFilename(),
            is("generalOrder.pdf"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is(GeneralOrderAddressTo.APPLICANT));

        Document latestGeneralOrder = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        assertThat(latestGeneralOrder.getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void generateGeneralOrderConsentedInContested() throws Exception {
        FinremCaseDetails caseDetails = consentedInContestedCaseDetails();
        generalOrderService.createAndSetGeneralOrder(AUTH_TOKEN, caseDetails);

        Document result = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderPreviewDocument();
        doCaseDocumentAssert(result);

        verifyAdditionalFieldsContested();
    }

    @Test
    public void submitConsentedInContestedGeneralOrder() throws Exception {
        FinremCaseDetails caseDetails = consentedInContestedCaseDetails();
        generalOrderService.populateGeneralOrderCollection(caseDetails);

        List<ContestedGeneralOrderCollection> generalOrders = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrdersConsent();

        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getFilename(),
            is("app_docs.pdf"));
        assertThat(generalOrders.get(0).getValue().getAdditionalDocument().getBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getValue().getAdditionalDocument().getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is(GeneralOrderAddressTo.APPLICANT));

        Document latestGeneralOrder = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        assertThat(latestGeneralOrder.getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void submitConsentedGeneralOrder() throws Exception {
        FinremCaseDetails caseDetails = consentedCaseDetails();
        generalOrderService.populateGeneralOrderCollection(caseDetails);
        List<GeneralOrderCollection> generalOrders = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderCollection();
        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getValue().getGeneralOrderDocumentUpload().getUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getValue().getGeneralOrderDocumentUpload().getFilename(),
            is("app_docs.pdf"));
        assertThat(generalOrders.get(0).getValue().getGeneralOrderDocumentUpload().getBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getValue().getGeneralOrderDocumentUpload().getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderDocumentUpload().getFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderDocumentUpload().getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is(GeneralOrderAddressTo.APPLICANT));

        Document latestGeneralOrder = caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        assertThat(latestGeneralOrder.getUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void addressToFormattedCorrectlyForApplicant() throws Exception {
        FinremCaseDetails details = consentedCaseDetails();
        details.getCaseData().getGeneralOrderWrapper().setGeneralOrderAddressTo(GeneralOrderAddressTo.APPLICANT);

        generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderCollection> generalOrders = details.getCaseData().getGeneralOrderWrapper().getGeneralOrderCollection();
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is(GeneralOrderAddressTo.APPLICANT));
    }

    @Test
    public void addressToFormattedCorrectlyForApplicantSolicitor() throws Exception {
        FinremCaseDetails details = consentedCaseDetails();
        details.getCaseData().getGeneralOrderWrapper().setGeneralOrderAddressTo(GeneralOrderAddressTo.APPLICANT_SOLICITOR);
        generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderCollection> generalOrders = details.getCaseData().getGeneralOrderWrapper().getGeneralOrderCollection();
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is(GeneralOrderAddressTo.APPLICANT_SOLICITOR));
    }

    @Test
    public void addressToFormattedCorrectlyForRespondentSolicitor() throws Exception {
        FinremCaseDetails details = consentedCaseDetails();
        details.getCaseData().getGeneralOrderWrapper().setGeneralOrderAddressTo(GeneralOrderAddressTo.RESPONDENT_SOLICITOR);
        generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderCollection> generalOrders = details.getCaseData().getGeneralOrderWrapper().getGeneralOrderCollection();
        assertThat(generalOrders.get(1).getValue().getGeneralOrderAddressTo(), is(GeneralOrderAddressTo.RESPONDENT_SOLICITOR));
    }

    @Test
    public void getsCorrectGeneralOrdersForPrintingConsented() throws Exception {
        FinremCaseDetails details = consentedCaseDetails();
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getCaseData());
        assertThat(latestGeneralOrder.getBinaryFileUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void getsZeroGeneralOrdersForPrintingWhenNoneConsented() throws Exception {
        FinremCaseDetails details = consentedCaseDetails();
        details.getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getCaseData());
        assertNull(latestGeneralOrder);
    }

    private FinremCaseDetails consentedCaseDetails() throws Exception {
        return deserializer.deserialize(new String(Files.readAllBytes(Paths.get("/fixtures/general-order-consented.json"))))
            .getCaseDetails();
    }

    private FinremCaseDetails contestedCaseDetails() throws Exception {
        return deserializer.deserialize(new String(Files.readAllBytes(Paths.get("/fixtures/general-order-contested.json"))))
            .getCaseDetails();
    }

    private static void doCaseDocumentAssert(Document result) {
        assertThat(result.getFilename(), is(FILE_NAME));
        assertThat(result.getUrl(), is(DOC_URL));
        assertThat(result.getBinaryUrl(), is(BINARY_URL));
    }

    private FinremCaseDetails consentedInContestedCaseDetails() throws Exception {
        return deserializer.deserialize(new String(Files.readAllBytes(Paths.get("/fixtures/general-order-consented-in-contested.json"))))
            .getCaseDetails();
    }

    void verifyAdditionalFieldsConsented() {
        verify(genericDocumentService, times(1))
            .generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
                eq(documentConfiguration.getGeneralOrderTemplate()), eq(documentConfiguration.getGeneralOrderFileName()));

        Map<String, Object> data = placeholdersMapCaptor.getValue();
        assertThat(data.get("DivorceCaseNumber"), is("DD12D12345"));
        assertThat(data.get("ApplicantName"), is("Consented Applicant Name"));
        assertThat(data.get("RespondentName"), is("Consented Respondent Name"));
        assertThat(data.get("GeneralOrderCourt"), is("SITTING in private"));
        assertThat(data.get("GeneralOrderJudgeDetails"), is("His Honour Judge Consented"));
        assertThat(data.get("GeneralOrderRecitals"), is("Consented Recitals"));
        assertThat(data.get("GeneralOrderDate"), is("01/01/2020"));
        assertThat(data.get("GeneralOrderBodyText"), is("Test is dummy text for consented"));
        assertThat(data.get("GeneralOrderHeaderOne"), is("Sitting in the Family Court"));
    }

    void verifyAdditionalFieldsContested() {
        verify(genericDocumentService, times(1))
            .generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
                eq(documentConfiguration.getGeneralOrderTemplate()), eq(documentConfiguration.getGeneralOrderFileName()));

        Map<String, Object> data = placeholdersMapCaptor.getValue();
        assertThat(data.get("DivorceCaseNumber"), is("DD98D76543"));
        assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
        assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
        assertThat(data.get("GeneralOrderCourt"), is("Nottingham County Court and Family Court"));
        assertThat(data.get("GeneralOrderJudgeDetails"), is("Her Honour Judge Contested"));
        assertThat(data.get("GeneralOrderRecitals"), is("Contested Recitals"));
        assertThat(data.get("GeneralOrderDate"), is("01/06/2020"));
        assertThat(data.get("GeneralOrderBodyText"), is("Test is dummy text for contested"));
        assertThat(data.get("GeneralOrderHeaderOne"), is("In the Family Court"));
        assertThat(data.get("GeneralOrderHeaderTwo"), is("sitting in the"));
        assertThat(data.get("GeneralOrderCourtSitting"), is("SITTING AT the Family Court at the "));

    }
}
