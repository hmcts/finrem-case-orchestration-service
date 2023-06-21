package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;

public class IntervenerOneCaseSummariesHandlerTest  extends CaseDocumentHandlerTest {

    private CaseDetails caseDetails;
    private Map<String, Object> caseData;
    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    IntervenerOneCaseSummariesHandler handler = new IntervenerOneCaseSummariesHandler(mapper);

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void appCaseSummariesFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", INTERVENER_ONE, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", INTERVENER_ONE, "no", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", INTERVENER_ONE, "no", "no", null));

        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        handler.handle(uploadDocumentList, caseData);

        assertThat(getDocumentCollection(caseData, INTV_ONE_CASE_SUMMARIES_COLLECTION), hasSize(3));
    }
}
