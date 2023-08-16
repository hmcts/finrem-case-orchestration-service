package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@RunWith(MockitoJUnitRunner.class)
public class FinremFormCandGCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);

    @Mock
    private GenericDocumentService genericDocumentService;

    private static final String DATE_OF_HEARING = "2019-01-01";

    @Before
    public void setUp() throws Exception {
        caseDetails = caseDetails();
        applicantAndRespondentMultiLetterCorresponder =
            new FinremFormCandGCorresponder(bulkPrintService, notificationService,
                new DocumentHelper(objectMapper, new CaseDataService(objectMapper), genericDocumentService, finremCaseDetailsMapper));
    }

    @Test
    public void getDocumentsToPrint() {
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(5, documentsToPrint.size());
    }

    private FinremCaseDetails caseDetails() {
        FinremCaseData caseData = FinremCaseData.builder()
            .hearingDate(LocalDate.parse(DATE_OF_HEARING))
            .fastTrackDecision(YesOrNo.forValue(NO_VALUE))
            .formC(caseDocument())
            .formG(caseDocument())
            .copyOfPaperFormA(List.of(
                PaymentDocumentCollection.builder()
                    .value(PaymentDocument.builder()
                        .typeOfDocument(PaymentDocumentType.COPY_OF_PAPER_FORM_A)
                        .uploadedDocument(caseDocument())
                        .build())
                    .build()))
            .outOfFamilyCourtResolution(caseDocument())
            .additionalListOfHearingDocuments(caseDocument())
            .build();
        return FinremCaseDetails.builder().id(1234L).data(caseData).build();
    }
}