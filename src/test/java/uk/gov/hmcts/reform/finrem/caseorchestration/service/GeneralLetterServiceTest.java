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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;

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
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Applicant Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("test Korivi"));
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
    }

    @Test
    public void generateContestedGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Applicant Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SW1V 4FG"));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("Sarah Beatrice Korivi"));
    }

    @Test
    public void givenNoPreviousGeneralLettersGenerated_generateGeneralLetter() throws Exception {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter-empty-collection.json", mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(1));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());

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
                GeneralLetterAddressToType.APPLICANT_SOLICITOR, "Solictor",
                GeneralLetterAddressToType.RESPONDENT_SOLICITOR, "Ms Patel",
                GeneralLetterAddressToType.RESPONDENT, "test Korivi",
                GeneralLetterAddressToType.OTHER, "Mr Rajesh Kuthrappali",
                GeneralLetterAddressToType.APPLICANT, "Poor Guy"
            ).entrySet().stream()
            .forEach(entry -> assertNameUsedForGeneralLetterAddressTo(invocationCounter.getAndIncrement(), entry.getKey(), entry.getValue()));
    }

    @Test
    public void whenGeneralLetterPreviewCalled_thenPreviewDocumentIsAddedToCaseData() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);

        assertNull(caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());

        generalLetterService.previewGeneralLetter(AUTH_TOKEN, caseDetails);
        assertNotNull(caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
    }

    @Test
    public void givenAddressIsMissing_whenCaseDataErrorsFetched_ThereIsAnError() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter-missing-address.json", mapper);

        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, hasItem("Address is missing for recipient type respondent"));
    }

    @Test
    public void givenAddressIsPresent_whenCaseDataErrorsFetched_ThereIsNoError() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);

        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, is(empty()));
    }

    @Test
    public void whenGeneralLetterIsCreated_thenItGetsSentToBulkPrint() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verify(bulkPrintService, times(1)).bulkPrintFinancialRemedyLetterPack(anyLong(), any(), any(), any());
    }

    private void assertNameUsedForGeneralLetterAddressTo(int invocation, GeneralLetterAddressToType generalLetterAddressTo, String expectedName) {
        FinremCaseDetails caseDetails;
        try {
            caseDetails = caseDetails();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e.getCause());
        }
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressTo(generalLetterAddressTo);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(invocation)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Addressee addressee = (Addressee) documentGenerationRequestCaseDetailsCaptor.getValue().getData().get(ADDRESSEE);
        assertThat(addressee.getName(), is(expectedName));
    }

    private FinremCaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-letter.json")) {
            return mapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
        }
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}
