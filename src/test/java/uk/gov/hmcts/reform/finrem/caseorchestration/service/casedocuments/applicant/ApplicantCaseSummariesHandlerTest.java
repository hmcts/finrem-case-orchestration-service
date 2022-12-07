package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantCaseSummariesHandlerTest extends CaseDocumentCollectionsManagerTest {

    ApplicantCaseSummariesCollectionService applicantCaseSummariesHandler =
        new ApplicantCaseSummariesCollectionService(evidenceManagementDeleteService);

    @Test
    public void appCaseSummariesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.POSITION_STATEMENT,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.SKELETON_ARGUMENT,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CASE_SUMMARY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        applicantCaseSummariesHandler.processUploadDocumentCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollection(ManageCaseDocumentsCollectionType.APP_CASE_SUMMARIES_COLLECTION),
            hasSize(3));
    }

}