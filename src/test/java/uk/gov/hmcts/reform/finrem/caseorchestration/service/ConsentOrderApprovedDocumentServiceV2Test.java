package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;

@ActiveProfiles("test-mock-feign-clients-generic")
public class ConsentOrderApprovedDocumentServiceV2Test extends BaseServiceTest {

    private static final String DEFAULT_COVERSHEET_URL = "defaultCoversheetUrl";
    private static final String DOC_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    private static final String BINARY_URL = DOC_URL + "/binary";
    private static final String FILE_NAME = "app_docs.docx";
    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private DocumentHelper documentHelper;
    @Autowired
    private DocmosisPdfGenerationService documentClientMock;
    @Autowired
    private GenericDocumentService genericDocumentService;

    @Value("${document.bulkPrintTemplate}")
    private String documentBulkPrintTemplate;
    @Value("${document.bulkPrintFileName}")
    private String documentBulkPrintFileName;

    @Value("${document.approvedConsentOrderTemplate}")
    private String documentApprovedConsentOrderTemplate;
    @Value("${document.approvedConsentOrderFileName}")
    private String documentApprovedConsentOrderFileName;

    @Value("${document.approvedVariationOrderFileName}")
    private String approvedVariationOrderFileName;

    @Value("${document.approvedConsentOrderNotificationTemplate}")
    private String documentApprovedConsentOrderNotificationTemplate;
    @Value("${document.approvedConsentOrderNotificationFileName}")
    private String documentApprovedConsentOrderNotificationFileName;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = defaultConsentedCaseDetails();

        Document defaultCoversheet = document();
        defaultCoversheet.setBinaryUrl(DEFAULT_COVERSHEET_URL);

        Document consentOrderApprovedCoverLetter = document();
        consentOrderApprovedCoverLetter.setBinaryUrl(CONSENT_ORDER_APPROVED_COVER_LETTER_URL);

    }

    @Test
    public void whenPreparingApplicantLetterPack() throws Exception {
        Mockito.reset(genericDocumentService);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        CaseDetails caseDetailsTemp = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(caseDocument(DOC_URL,documentApprovedConsentOrderFileName,BINARY_URL));
        addConsentOrderApprovedDataToCaseDetails(caseDetailsTemp);

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService
            .prepareApplicantLetterPack(caseDetailsTemp, AUTH_TOKEN);

        assertThat(documents, hasSize(3));
        assertThat(documents.get(0).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void whenPreparingApplicantLetterPack_paperApplication() throws Exception {
        Mockito.reset(genericDocumentService);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        CaseDetails caseDetailsTemp = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        caseDetailsTemp.getData().put(PAPER_APPLICATION, YES_VALUE);
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(caseDocument(DOC_URL,documentApprovedConsentOrderFileName,BINARY_URL));
        addConsentOrderApprovedDataToCaseDetails(caseDetailsTemp);

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService
            .prepareApplicantLetterPack(caseDetailsTemp, AUTH_TOKEN);

        assertThat(documents, hasSize(4));
        assertThat(documents.get(0).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(documents.get(1).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(documents.get(2).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(documents.get(3).getBinaryFileUrl(), is(BINARY_URL));
    }

    private List<CaseDocument> getDocumentList(Map<String, Object> data) {
        return mapper.convertValue(data.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<>() {
        });
    }

    private void addConsentOrderApprovedDataToCaseDetails(CaseDetails caseDetails) throws Exception {

        ApprovedOrder.ApprovedOrderBuilder builder = ApprovedOrder.builder();
        builder.orderLetter(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));
        builder.consentOrder(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));


        List<PensionTypeCollection> pensionTypeCollections = new ArrayList<>();
        PensionType pensionType = PensionType.builder().typeOfDocument(PensionDocumentType.FORM_PPF1)
            .pensionDocument(caseDocument()).build();
        PensionTypeCollection typeCollection = PensionTypeCollection.builder().typedCaseDocument(pensionType).build();
        pensionTypeCollections.add(typeCollection);
        builder.pensionDocuments(pensionTypeCollections);

        List<ConsentOrderCollection> approvedOrderCollection = new ArrayList<>();
        approvedOrderCollection.add(ConsentOrderCollection.builder().approvedOrder(builder.build()).build());

        caseDetails.getData().put(APPROVED_ORDER_COLLECTION, approvedOrderCollection);
    }
}
