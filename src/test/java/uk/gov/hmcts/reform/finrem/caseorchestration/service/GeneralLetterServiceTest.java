package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;

@ActiveProfiles("test-mock-document-client")
public class GeneralLetterServiceTest extends BaseServiceTest {

    @Autowired private GeneralLetterService generalLetterService;
    @Autowired private ObjectMapper mapper;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @Test
    public void generateGeneralLetter() throws Exception {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());

        CaseDetails caseDetails = caseDetails();
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterData> generalLetterData = (List<GeneralLetterData>) caseDetails.getData().get(GENERAL_LETTER);
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getGeneralLetter().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getGeneralLetter().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-letter.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}
