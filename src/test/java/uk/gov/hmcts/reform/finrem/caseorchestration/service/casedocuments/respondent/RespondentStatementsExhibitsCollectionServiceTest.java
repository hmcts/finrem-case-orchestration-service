package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsServiceTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentStatementsExhibitsCollectionServiceTest extends CaseDocumentCollectionsServiceTest {

    RespondentStatementsExhibitsCollectionService collectionService =
        new RespondentStatementsExhibitsCollectionService(evidenceManagementDeleteService);

    @Test
    public void respStatementsExhibitsFiltered() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));


        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.processUploadDocumentCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseDetails.getData().getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection(),
            hasSize(2));
    }
}