package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantChronologiesStatementHandlerTest extends CaseDocumentCollectionsManagerTest {

    ApplicantChronologiesStatementCollectionService applicantChronologiesStatementHandler =
        new ApplicantChronologiesStatementCollectionService(evidenceManagementDeleteService);

    @Test
    public void appChronologiesStatementsFiltered() {

        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantChronologiesStatementHandler.processUploadDocumentCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollection(ManageCaseDocumentsCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            hasSize(3));
    }
}