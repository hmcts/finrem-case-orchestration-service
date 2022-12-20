package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.additionalhearing.AdditionalHearingApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.additionalhearing.AdditionalHearingCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.additionalhearing.AdditionalHearingDocumentGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.additionalhearing.AdditionalHearingRespondentCorresponder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalHearingCorrespondenceTest extends HearingCorrespondenceBaseTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private AdditionalHearingApplicantCorresponder additionalHearingApplicantCorresponder;
    private AdditionalHearingRespondentCorresponder additionalHearingRespondentCorresponder;
    private AdditionalHearingDocumentGenerator additionalHearingDocumentService;

    @Before
    public void setUpTest() {

        additionalHearingDocumentService = new AdditionalHearingDocumentGenerator(documentHelper);

        additionalHearingApplicantCorresponder =
            new AdditionalHearingApplicantCorresponder(notificationService, bulkPrintService, additionalHearingDocumentService);

        additionalHearingRespondentCorresponder =
            new AdditionalHearingRespondentCorresponder(notificationService, bulkPrintService, additionalHearingDocumentService);

        applicantAndRespondentMultiLetterCorresponder =
            new AdditionalHearingCorresponder(additionalHearingApplicantCorresponder, additionalHearingRespondentCorresponder);
        caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);

        when(documentHelper.convertToAdditionalHearingDocumentData(any())).thenReturn(getAdditionalHearingDocumentData());
        when(documentHelper.getBulkPrintDocumentFromCaseDocument(any())).thenReturn(getBulkPrintDocument());
        when(documentHelper.convertToCaseDocument(any())).thenReturn(getCaseDocument());

    }

    private CaseDocument getCaseDocument() {
        return CaseDocument.builder().documentFilename("test").documentBinaryUrl("test").documentUrl("test").build();
    }


    private List<AdditionalHearingDocumentData> getAdditionalHearingDocumentData() {
        return List.of(AdditionalHearingDocumentData.builder().additionalHearingDocument(AdditionalHearingDocument.builder().build()).build(),
            AdditionalHearingDocumentData.builder().additionalHearingDocument(AdditionalHearingDocument.builder().build()).build());
    }
}