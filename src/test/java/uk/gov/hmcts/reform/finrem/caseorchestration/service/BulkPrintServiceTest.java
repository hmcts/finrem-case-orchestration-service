package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.print.BulkPrintMetadata;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.shouldBeSentToApplicant;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class BulkPrintServiceTest {

    @Mock
    private DocumentClient documentClientMock;

    @Mock
    private GenerateCoverSheetService generateCoverSheetServiceMock;

    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestArgumentCaptor;

    private BulkPrintService service;

    private CaseDetails caseDetails;
    private CaseDocument caseDocument;
    private UUID letterId;

    @Before
    public void setUp() {
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(BulkPrintRequest.class);
        DocumentConfiguration config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("test_template");
        config.setApprovedConsentOrderFileName("test_file");
        documentClientMock = mock(DocumentClient.class);
        service = new BulkPrintService(documentClientMock, config, new ObjectMapper(), generateCoverSheetServiceMock);
    }

    @Test
    public void sendLetterToApplicantReturnsExpectedResult() {
        String party = "applicant";
        prepareDataForTest(party);

        when(generateCoverSheetServiceMock.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN))
                .thenReturn(caseDocument);

        BulkPrintMetadata result = service.sendLetterToApplicant(AUTH_TOKEN, caseDetails);

        assertOutput(result, party);
    }

    @Test
    public void sendLetterToRespondentReturnsExpectedResult() {
        String party = "respondent";
        prepareDataForTest(party);

        when(generateCoverSheetServiceMock.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN))
                .thenReturn(caseDocument);

        BulkPrintMetadata result = service.sendLetterToRespondent(AUTH_TOKEN, caseDetails);

        assertOutput(result, party);
    }

    @Test
    public void shouldBeSentToApplicantReturnsTrueWhenApplicantRepresentedIsNo() {
        ImmutableMap<String, Object> caseData = ImmutableMap.of(APPLICANT_REPRESENTED, NO_VALUE);

        assertThat(shouldBeSentToApplicant(caseData), is(true));
    }

    @Test
    public void shouldBeSentToApplicantReturnsTrueWhenApplicantRepresentedIsNoSolicitorAgreeToReceiveEmailsDoesntMatter() {
        ImmutableMap<String, Object> caseData = ImmutableMap.of(APPLICANT_REPRESENTED, NO_VALUE);

        Stream.of("", YES_VALUE, "this should be 'yes' or 'no'").forEach(value -> {
            assertThat(
                shouldBeSentToApplicant(
                    ImmutableMap.of(
                        APPLICANT_REPRESENTED, NO_VALUE,
                        SOLICITOR_AGREE_TO_RECEIVE_EMAILS, value
                    )
                ), is(true));
        });
    }

    @Test
    public void shouldBeSentToApplicantReturnsFalseWhenApplicantRepresentedIsAProvidedValue() {
        Stream.of("", YES_VALUE, "this should be 'yes' or 'no'").forEach(value -> {
            assertThat(shouldBeSentToApplicant( ImmutableMap.of(APPLICANT_REPRESENTED, value)), is(false));
        });
    }

    @Test
    public void shouldBeSentToApplicantReturnsTrueWhenSolicitorAgreeToReceiveEmailsIsNo() {
        Stream.of("", YES_VALUE, "this should be 'yes' or 'no'").forEach(value -> {
            assertThat(
                shouldBeSentToApplicant(
                    ImmutableMap.of(
                        APPLICANT_REPRESENTED, value,
                        SOLICITOR_AGREE_TO_RECEIVE_EMAILS, NO_VALUE
                    )
                ), is(true));
        });
    }

    private void assertOutput(BulkPrintMetadata result, String party) {
        List<BulkPrintDocument> bulkPrintDocuments = bulkPrintRequestArgumentCaptor.getValue().getBulkPrintDocuments();

        assertThat(result.getCoverSheet(), is(caseDocument));
        assertThat(result.getLetterId(), is(letterId));
        assertThat(bulkPrintDocuments.size(), is(1));
        assertThat(bulkPrintDocuments.get(0).getBinaryFileUrl(), is(getDocumentBinaryUrl(party)));
    }

    private void prepareDataForTest(String party) {
        caseDetails = CaseDetails.builder().data(new HashMap<>()).id(1L).build();
        caseDocument = buildCaseDocument(party);
        letterId = UUID.randomUUID();

        when(documentClientMock.bulkPrint(bulkPrintRequestArgumentCaptor.capture())).thenReturn(letterId);
    }

    @NotNull
    private static CaseDocument buildCaseDocument(String party) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(getDocumentBinaryUrl(party));
        caseDocument.setDocumentFilename(getDocumentFilename(party));
        caseDocument.setDocumentUrl(getDocumentUrl(party));

        return caseDocument;
    }

    @NotNull
    private static String getDocumentBinaryUrl(String party) {
        return party + "-binary-url";
    }

    @NotNull
    private static String getDocumentFilename(String party) {
        return party + "-document-filename";
    }

    @NotNull
    private static String getDocumentUrl(String party) {
        return party + "-doc-url";
    }
}
