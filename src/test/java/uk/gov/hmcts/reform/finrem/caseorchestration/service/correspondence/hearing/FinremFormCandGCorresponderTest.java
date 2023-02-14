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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;

@RunWith(MockitoJUnitRunner.class)
public class FinremFormCandGCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
    @Mock
    private GenericDocumentService genericDocumentService;
    private static final String DATE_OF_HEARING = "2019-01-01";

    @Before
    public void setUp() throws Exception {
        caseDetails = caseDetails(NO_VALUE);
        applicantAndRespondentMultiLetterCorresponder =
            new FinremFormCandGCorresponder(bulkPrintService, notificationService,
                new DocumentHelper(objectMapper, new CaseDataService(), genericDocumentService, finremCaseDetailsMapper), objectMapper);
    }

    @Test
    public void getDocumentsToPrint() {
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(5, documentsToPrint.size());
    }

    private FinremCaseDetails caseDetails(String isFastTrackDecision) {
        FinremCaseData caseData = FinremCaseData.builder()
            .hearingDate(LocalDate.parse(DATE_OF_HEARING))
            .fastTrackDecision(YesOrNo.forValue(isFastTrackDecision))
            .formC(caseDocument())
            .formG(caseDocument())
            .copyOfPaperFormA(singletonList(pensionDocumentData()))
            .outOfFamilyCourtResolution(caseDocument())
            .additionalListOfHearingDocuments(caseDocument())
            .build();
        return FinremCaseDetails.builder().id(1234L).data(caseData).build();
    }
}