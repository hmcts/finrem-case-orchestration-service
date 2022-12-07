package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManagerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RespondentStatementsExhibitsHandlerTest extends CaseDocumentCollectionsManagerTest {

    RespondentStatementsExhibitsCollectionService respondentStatementsExhibitsHandler = new RespondentStatementsExhibitsCollectionService();

    @Test
    public void respStatementsExhibitsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_AFFIDAVIT, CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT, CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));


        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);

        respondentStatementsExhibitsHandler.processUploadDocumentCollection(caseData);

        assertThat(caseDetails.getData().getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection(), hasSize(2));
    }
}