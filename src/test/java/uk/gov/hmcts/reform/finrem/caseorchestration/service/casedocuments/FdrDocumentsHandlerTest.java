package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_FDR_DOCS_COLLECTION;

public class FdrDocumentsHandlerTest extends CaseDocumentHandlerTest {

    FdrDocumentsHandler fdrDocumentsHandler = new FdrDocumentsHandler(new ObjectMapper());

    @Test
    public void shouldFilterFdrDocumentsWhenPartyIsResp() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
        assertNull(getDocumentCollection(caseData, INTV_ONE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_TWO_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_THREE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_FOUR_FDR_DOCS_COLLECTION));
    }

    @Test
    public void giveContestedCase_whenPartyIsIntervenerOne_theCopyDocInIntv1AndMainFdrCollection() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "intervener1",
            "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_ONE_FDR_DOCS_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
        assertNull(getDocumentCollection(caseData, INTV_TWO_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_THREE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_FOUR_FDR_DOCS_COLLECTION));
    }

    @Test
    public void giveContestedCase_whenPartyIsIntervenerTwo_theCopyDocInIntv2AndMainFdrCollection() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "intervener2",
            "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_TWO_FDR_DOCS_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
        assertNull(getDocumentCollection(caseData, INTV_ONE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_THREE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_FOUR_FDR_DOCS_COLLECTION));
    }

    @Test
    public void giveContestedCase_whenPartyIsIntervenerThree_theCopyDocInIntv3AndMainFdrCollection() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "intervener3",
            "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_THREE_FDR_DOCS_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
        assertNull(getDocumentCollection(caseData, INTV_ONE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_TWO_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_FOUR_FDR_DOCS_COLLECTION));
    }

    @Test
    public void giveContestedCase_whenPartyIsIntervenerFour_theCopyDocInIntv4AndMainFdrCollection() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "intervener4",
            "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_FOUR_FDR_DOCS_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
        assertNull(getDocumentCollection(caseData, INTV_ONE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_THREE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_TWO_FDR_DOCS_COLLECTION));
    }

    @Test
    public void giveContestedCase_whenPartyIsNotInterveners_theCopyDocFdrCollectionOnly() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant",
            "no", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        fdrDocumentsHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(1));
        assertNull(getDocumentCollection(caseData, INTV_ONE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_THREE_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_TWO_FDR_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, INTV_FOUR_FDR_DOCS_COLLECTION));
    }
}