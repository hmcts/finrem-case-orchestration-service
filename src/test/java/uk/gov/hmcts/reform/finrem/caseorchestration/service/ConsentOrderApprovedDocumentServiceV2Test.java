package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.FILE_URL;

@ActiveProfiles("test-mock-feign-clients-generic")
public class ConsentOrderApprovedDocumentServiceV2Test extends BaseServiceTest {

    private static final String DEFAULT_COVERSHEET_URL = "defaultCoversheetUrl";
    private static final String CONSENT_ORDER_APPROVED_COVER_LETTER_URL = "consentOrderApprovedCoverLetterUrl";

    @Autowired
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Autowired
    private DocumentHelper documentHelper;
    @Autowired
    private GenericDocumentService genericDocumentService;
    @Value("${document.approvedConsentOrderFileName}")
    private String documentApprovedConsentOrderFileName;

    private FinremCaseDetails finremCaseDetails;

    @Before
    public void setUp() {
        finremCaseDetails = defaultConsentedFinremCaseDetails();
        this.finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper());

        Document defaultCoversheet = document();
        defaultCoversheet.setBinaryUrl(DEFAULT_COVERSHEET_URL);

        Document consentOrderApprovedCoverLetter = document();
        consentOrderApprovedCoverLetter.setBinaryUrl(CONSENT_ORDER_APPROVED_COVER_LETTER_URL);
    }

    @Test
    public void whenPreparingApplicantLetterPack() {
        Mockito.reset(genericDocumentService);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());

        FinremCaseDetails finremCaseDetailsTemp = documentHelper.deepCopy(finremCaseDetails, FinremCaseDetails.class);
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(caseDocument(FILE_URL, documentApprovedConsentOrderFileName, BINARY_URL));
        addConsentOrderApprovedDataToCaseDetails(finremCaseDetailsTemp);
        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetailsTemp, AUTH_TOKEN, APPLICANT);

        assertThat(documents).isEmpty();
    }

    @Test
    public void whenPreparingApplicantLetterPack_paperApplication() {
        Mockito.reset(genericDocumentService);
        FinremCaseDetails finremCaseDetailsTemp = documentHelper.deepCopy(finremCaseDetails, FinremCaseDetails.class);
        finremCaseDetailsTemp.getData().setPaperApplication(YesOrNo.YES);
        CaseDocument generatedDocument = caseDocument(FILE_URL, documentApprovedConsentOrderFileName, BINARY_URL);
        addConsentOrderApprovedDataToCaseDetails(finremCaseDetailsTemp);

        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.generateDocument(any(), any(), any(), any()))
            .thenReturn(generatedDocument);

        List<BulkPrintDocument> documents = consentOrderApprovedDocumentService
            .addApprovedConsentOrderCoverLetter(finremCaseDetailsTemp, AUTH_TOKEN, APPLICANT);

        BulkPrintDocument expectedBulkPrintDocument = documentHelper.mapToBulkPrintDocument(generatedDocument);

        assertThat(documents).containsExactly(expectedBulkPrintDocument);
    }

    private void addConsentOrderApprovedDataToCaseDetails(FinremCaseDetails caseDetails) {
        ApprovedOrder.ApprovedOrderBuilder builder = ApprovedOrder.builder();
        builder.orderLetter(caseDocument());
        builder.consentOrder(caseDocument());

        List<PensionTypeCollection> pensionTypeCollections = new ArrayList<>();
        PensionType pensionType = PensionType.builder().typeOfDocument(PensionDocumentType.FORM_PPF1)
            .pensionDocument(caseDocument()).build();
        PensionTypeCollection typeCollection = PensionTypeCollection.builder().typedCaseDocument(pensionType).build();
        pensionTypeCollections.add(typeCollection);
        builder.pensionDocuments(pensionTypeCollections);

        List<ConsentOrderCollection> approvedOrderCollection = new ArrayList<>();
        approvedOrderCollection.add(ConsentOrderCollection.builder().approvedOrder(builder.build()).build());

        caseDetails.getData().setApprovedOrderCollection(approvedOrderCollection);
    }
}
