package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;

public abstract class NocLettersProcessorBaseTest {

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String COR_RESPONDENT = "Respondent";

    @Mock
    protected NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    @Mock
    protected SolicitorNocDocumentService solicitorNocDocumentService;
    @Mock
    protected CaseDataService caseDataService;
    @Mock
    protected BulkPrintService bulkPrintService;

    private final NoticeType noticeType;

    public NocLettersProcessorBaseTest(NoticeType noticeType) {
        this.noticeType = noticeType;
    }

    protected CaseDetails getCaseDetails(String resourcePath) {
        CaseDetails caseDetails =
            caseDetailsFromResource(resourcePath, new ObjectMapper());
        return caseDetails;
    }

    protected CaseDocument setUpCaseDocumentInteraction(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
                                                        NocDocumentService nocDocumentService, String docFileName) {
        CaseDocument caseDocument = CaseDocument.builder().documentFilename(docFileName).build();
        when(nocDocumentService.generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails)).thenReturn(caseDocument);
        return caseDocument;
    }

    protected NoticeOfChangeLetterDetails getNoticeOfChangeLetterDetails(CaseDetails caseDetails, RepresentationUpdate representationUpdate,
                                                                         DocumentHelper.PaperNotificationRecipient recipient) {
        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails = NoticeOfChangeLetterDetails.builder().build();
        when(noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, recipient,
            noticeType)).thenReturn(noticeOfChangeLetterDetails);
        return noticeOfChangeLetterDetails;
    }
}
