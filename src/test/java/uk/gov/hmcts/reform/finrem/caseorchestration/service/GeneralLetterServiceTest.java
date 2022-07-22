package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterCollection;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;

public class GeneralLetterServiceTest extends BaseServiceTest {

    public static final String GENERAL_LETTER_CONTESTED_JSON = "/fixtures/contested/general-letter-contested.json";
    public static final String GENERAL_LETTER_JSON = "/fixtures/general-letter.json";
    @Autowired
    private GeneralLetterService generalLetterService;
    @Autowired
    private ObjectMapper mapper;
    @Qualifier("finremCallbackRequestDeserializer")
    @Autowired
    private FinremCallbackRequestDeserializer deserializer;

    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<Map<String, Object>> documentGenerationRequestCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void generateGeneralLetter() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(GENERAL_LETTER_JSON), mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getCaseData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = getDataFromCaptor(documentGenerationRequestCaseDetailsCaptor);
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is("1234567890"));
        assertThat(mapper.convertValue(data.get(ADDRESSEE), Addressee.class).getFormattedAddress(), is("""
            50 Applicant Solicitor Street
            Second Address Line
            Greater London
            London
            SE12 9SE"""));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("test Korivi"));
    }

    @Test
    public void generateContestedGeneralLetter() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(GENERAL_LETTER_CONTESTED_JSON), mapper);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getCaseData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = getDataFromCaptor(documentGenerationRequestCaseDetailsCaptor);
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is("1234567890"));
        System.out.println(data.get(ADDRESSEE));
        assertThat(mapper.convertValue(data.get(ADDRESSEE), Addressee.class).getFormattedAddress(), is("""
            50 Applicant Solicitor Street
            Second Address Line
            Greater London
            London
            SW1V 4FG"""));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("Sarah Beatrice Korivi"));
    }

    @Test
    public void givenNoPreviousGeneralLettersGenerated_generateGeneralLetter() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource("/fixtures/general-letter-empty-collection.json"), mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONSENTED);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getCaseData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(1));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = getDataFromCaptor(documentGenerationRequestCaseDetailsCaptor);
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is("1234567891"));
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
            .forEach(entry -> assertNameUsedForGeneralLetterAddressTo(invocationCounter.getAndIncrement(),
                entry.getKey(), entry.getValue()));
    }

    @Test
    public void whenGeneralLetterPreviewCalled_thenPreviewDocumentIsAddedToCaseData() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource("/fixtures/general-letter.json"), mapper);

        assertThat(caseDetails.getCaseData().getGeneralLetterWrapper().getGeneralLetterPreview(), is(nullValue()));

        generalLetterService.previewGeneralLetter(AUTH_TOKEN, caseDetails);
        assertThat(caseDetails.getCaseData().getGeneralLetterWrapper().getGeneralLetterPreview(), is(notNullValue()));
    }

    @Test
    public void givenAddressIsMissing_whenCaseDataErrorsFetched_ThereIsAnError() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource("/fixtures/general-letter-missing-address.json"), mapper);

        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, hasItem("Address is missing for recipient type respondent"));
    }

    @Test
    public void givenAddressIsPresent_whenCaseDataErrorsFetched_ThereIsNoError() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource("/fixtures/general-letter.json"), mapper);

        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, is(empty()));
    }

    @Test
    public void whenGeneralLetterIsCreated_thenItGetsSentToBulkPrint() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource("/fixtures/general-letter.json"), mapper);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verify(bulkPrintService, times(1)).sendDocumentForPrint(any(Document.class), any());
    }

    private void assertNameUsedForGeneralLetterAddressTo(int invocation, String generalLetterAddressTo, String expectedName) {
        FinremCaseDetails caseDetails;
        try {
            caseDetails = caseDetails();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e.getCause());
        }
        caseDetails.getCaseData().getGeneralLetterWrapper()
            .setGeneralLetterAddressTo(GeneralLetterAddressToType.forValue(generalLetterAddressTo));
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(invocation)).generateDocumentFromPlaceholdersMap(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> data = getDataFromCaptor(documentGenerationRequestCaseDetailsCaptor);
        Addressee addressee = mapper.convertValue(data.get(ADDRESSEE), Addressee.class);
        assertThat(addressee.getName(), is(expectedName));
    }

    private FinremCaseDetails caseDetails() throws Exception {
        return deserializer.deserialize(getResource("/fixtures/general-letter.json"))
            .getCaseDetails();
    }

    private static void doCaseDocumentAssert(Document result) {
        assertThat(result.getFilename(), is(FILE_NAME));
        assertThat(result.getUrl(), is(DOC_URL));
        assertThat(result.getBinaryUrl(), is(BINARY_URL));
    }
}
