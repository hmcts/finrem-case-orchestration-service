package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

public class ApplicantHearingBundleHandlerTest extends CaseDocumentHandlerTest {

    ApplicantHearingBundleHandler applicantHearingBundleHandler = new ApplicantHearingBundleHandler(new ObjectMapper());

    @Test
    public void appHearingBundlesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "applicant", "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        applicantHearingBundleHandler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, APP_HEARING_BUNDLES_COLLECTION), hasSize(1));
    }
}