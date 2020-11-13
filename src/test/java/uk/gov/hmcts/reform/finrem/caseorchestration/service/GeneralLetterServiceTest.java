package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_PREVIEW;

public class GeneralLetterServiceTest extends BaseServiceTest {

    @Autowired
    private GeneralLetterService generalLetterService;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateGeneralLetter() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);
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
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Applicant Solicitor Street\n"
            + "Second Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("test Korivi"));
    }

    @Test
    public void generateContestedGeneralLetter() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
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
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Applicant Solicitor Street\n"
            + "Second Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SW1V 4FG"));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("Sarah Beatrice Korivi"));
    }

    @Test
    public void givenNoPreviousGeneralLettersGenerated_generateGeneralLetter() throws Exception {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter-empty-collection.json", mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterData> generalLetterData = (List<GeneralLetterData>) caseDetails.getData().get(GENERAL_LETTER);
        assertThat(generalLetterData, hasSize(1));

        doCaseDocumentAssert(generalLetterData.get(0).getGeneralLetter().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567891L));
    }

    @Test
    public void whenGeneralLetterAddressToChanges_differentNamesAreUsed() {
        AtomicInteger invocationCounter = new AtomicInteger(1);
        ImmutableMap.of(
            "applicantSolicitor", "Solictor",
            "respondentSolicitor", "Ms Patel",
            "respondent", "test Korivi",
            "other", "Mr Rajesh Kuthrappali"
        ).entrySet().stream()
            .forEach(entry -> assertNameUsedForGeneralLetterAddressTo(invocationCounter.getAndIncrement(), entry.getKey(), entry.getValue()));
    }

    @Test
    public void whenGeneralLetterPreviewCalled_thenPreviewDocumentIsAddedToCaseData() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);

        assertThat(caseDetails.getData(), not(hasKey(GENERAL_LETTER_PREVIEW)));

        generalLetterService.previewGeneralLetter(AUTH_TOKEN, caseDetails);
        assertThat(caseDetails.getData(), hasKey(GENERAL_LETTER_PREVIEW));
    }

    @Test
    public void givenAddressIsMissing_whenCaseDataErrorsFetched_ThereIsAnError() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter-missing-address.json", mapper);

        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, hasItem("Address is missing for recipient type respondent"));
    }

    @Test
    public void givenAddressIsPresent_whenCaseDataErrorsFetched_ThereIsNoError() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);

        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, is(empty()));
    }

    @Test
    public void whenGeneralLetterIsCreated_thenItGetsSentToBulkPrint() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/general-letter.json", mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verify(bulkPrintService, times(1)).sendDocumentForPrint(any(CaseDocument.class), any());
    }

    private void assertNameUsedForGeneralLetterAddressTo(int invocation, String generalLetterAddressTo, String expectedName) {
        CaseDetails caseDetails;
        try {
            caseDetails = caseDetails();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e.getCause());
        }
        caseDetails.getData().put("generalLetterAddressTo", generalLetterAddressTo);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(invocation)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Addressee addressee = (Addressee) documentGenerationRequestCaseDetailsCaptor.getValue().getData().get(ADDRESSEE);
        assertThat(addressee.getName(), is(expectedName));
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
