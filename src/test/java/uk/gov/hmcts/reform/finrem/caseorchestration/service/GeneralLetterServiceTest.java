package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;

public class GeneralLetterServiceTest extends BaseServiceTest {

    private static final String APP_SOLICITOR_LABEL = "Applicant Solicitor";
    private static final String RESP_SOLICITOR_LABEL = "Respondent Solicitor";
    private static final String INTV1_LABEL = "Intervener 1";
    private static final String INTV2_LABEL = "Intervener 2";
    private static final String INTV3_LABEL = "Intervener 3";
    private static final String INTV4_LABEL = "Intervener 4";
    private static final String INTV1_SOLICITOR_LABEL = "Intervener 1 Solicitor";
    private static final String INTV2_SOLICITOR_LABEL = "Intervener 2 Solicitor";
    private static final String INTV3_SOLICITOR_LABEL = "Intervener 3 Solicitor";
    private static final String INTV4_SOLICITOR_LABEL = "Intervener 4 Solicitor";
    private static final String GENERAL_LETTER_PATH = "/fixtures/general-letter.json";
    private static final String GENERAL_LETTER_CONTESTED_PATH = "/fixtures/contested/general-letter-contested.json";
    private static final String GENERAL_LETTER_MISSING_ADDRESS_PATH = "/fixtures/general-letter-missing-address.json";
    private static final String GENERAL_LETTER_EMPTY_COLLECTION_PATH = "/fixtures/general-letter-empty-collection.json";

    @Autowired
    private GeneralLetterService generalLetterService;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private BulkPrintDocumentService bulkPrintDocumentService;

    CaseDocument uploadedDocument;
    FinremCaseDetails generalLetterCaseDetails;
    FinremCaseDetails generalLetterContestedCaseDetails;

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        uploadedDocument = caseDocument(
            "http://document-management-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af69",
            "example_document.pdf",
            "http://document-management-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af69/binary");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(uploadedDocument);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn("Moj Resp");
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    public void shouldGenerateGeneralLetterForApplicant() {
        testGenerateGeneralLetterForLitigant(APPLICANT, "50 Applicant Street\n");
    }

    @Test
    public void shouldGenerateGeneralLetterForRespondent() {
        testGenerateGeneralLetterForLitigant(RESPONDENT, "50 Respondent Street\n");
    }

    @Test
    public void shouldGenerateGeneralLetterForApplicantSolicitor() {
        testGenerateGeneralLetterForLitigant(APPLICANT_SOLICITOR, "50 Applicant Solicitor Street\n");
    }

    @Test
    public void shouldGenerateGeneralLetterForRespondentSolicitor() {
        testGenerateGeneralLetterForLitigant(RESPONDENT_SOLICITOR, "50 Respondent Solicitor Street\n");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener1() {
        testGenerateGeneralLetterForIntervener(INTERVENER1, INTV1_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_1, "intvr1");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener1Solicitor() {
        testGenerateGeneralLetterForIntervener(INTERVENER1_SOLICITOR, INTV1_SOLICITOR_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_1, "intvr1sol");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener2() {
        testGenerateGeneralLetterForIntervener(INTERVENER2, INTV2_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_2, "intvr2");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener2Solicitor() {
        testGenerateGeneralLetterForIntervener(INTERVENER2_SOLICITOR, INTV2_SOLICITOR_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_2, "intvr2sol");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener3() {
        testGenerateGeneralLetterForIntervener(INTERVENER3, INTV3_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_3, "intvr3");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener3Solicitor() {
        testGenerateGeneralLetterForIntervener(INTERVENER3_SOLICITOR, INTV3_SOLICITOR_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_3, "intvr3sol");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener4() {
        testGenerateGeneralLetterForIntervener(INTERVENER4, INTV4_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_4, "intvr4");
    }

    @Test
    public void shouldGenerateGeneralLetterForIntervener4Solicitor() {
        testGenerateGeneralLetterForIntervener(INTERVENER4_SOLICITOR, INTV4_SOLICITOR_LABEL, DocumentCategory.CORRESPONDENCE_INTERVENER_4, "intvr4sol");
    }

    @Test
    public void givenNoPreviousGeneralLettersGenerated_shouldGenerateGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_EMPTY_COLLECTION_PATH);
        DynamicRadioList addresseeList = getDynamicRadioList(APPLICANT_SOLICITOR, APP_SOLICITOR_LABEL, false);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterUploadedDocument(caseDocument());
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();

        assertThat(generalLetterData, hasSize(1));
        verifyDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(), null, caseDocument());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument(), AUTH_TOKEN, String.valueOf(caseDetails.getId()));
        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567891L));
    }

    @Test
    public void whenGeneralLetterPreviewCalled_thenPreviewDocumentIsAddedToCaseData() {
        FinremCaseDetails generalLetterCaseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_PATH);
        assertNull(generalLetterCaseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
        FinremCaseData caseData = generalLetterCaseDetails.getData();
        DynamicRadioList addresseeList = getDynamicRadioList(OTHER_RECIPIENT, OTHER_RECIPIENT, false);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.previewGeneralLetter(AUTH_TOKEN, generalLetterCaseDetails);
        assertNotNull(generalLetterCaseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
    }

    @Test
    public void givenAddressIsMissing_whenCaseDataErrorsFetched_ThereIsAnError() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_MISSING_ADDRESS_PATH);
        DynamicRadioList addresseeList = getDynamicRadioList(RESPONDENT, RESPONDENT, true);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, hasItem("Address is missing for recipient type " + RESPONDENT));
    }

    @Test
    public void givenAddressIsPresent_whenCaseDataErrorsFetched_ThereIsNoError() {
        FinremCaseDetails generalLetterCaseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_PATH);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        generalLetterCaseDetails.getData().getGeneralLetterWrapper().setGeneralLetterRecipientAddress(getAddress());
        generalLetterCaseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(generalLetterCaseDetails);
        assertThat(errors, is(empty()));
    }

    @Test
    public void whenGeneralLetterIsCreated_thenItGetsSentToBulkPrint() {
        FinremCaseDetails generalLetterCaseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_PATH);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        generalLetterCaseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterCaseDetails.getData().setCcdCaseType(CaseType.CONTESTED);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, generalLetterCaseDetails);
        GeneralLetterWrapper wrapper = generalLetterCaseDetails.getData().getGeneralLetterWrapper();
        List<GeneralLetterCollection> generalLetterData = wrapper.getGeneralLetterCollection();
        assertNull(generalLetterCaseDetails.getData().getCourtDetails());
        verifyDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(),
            DocumentCategory.CORRESPONDENCE_OTHER.getDocumentCategoryId(), caseDocument());
        wrapper.getGeneralLetterUploadedDocuments().forEach(doc -> verifyDocumentFields(doc.getValue(),
            DocumentCategory.CORRESPONDENCE_OTHER.getDocumentCategoryId(), uploadedDocument));
        verify(bulkPrintService, times(1)).bulkPrintFinancialRemedyLetterPack(anyLong(), any(), any(), any());
    }

    @Test
    public void validateEncryptionOnUploadedDocuments() {
        List<DocumentCollection> caseDocuments = new ArrayList<>();
        DocumentCollection doc1 = DocumentCollection.builder().value(caseDocument()).build();
        caseDocuments.add(doc1);
        DocumentCollection doc2 = DocumentCollection.builder().value(null).build();
        caseDocuments.add(doc2);
        DocumentCollection doc3 = null;
        caseDocuments.add(doc3);
        String caseId = "1346347334";
        String auth = AUTH_TOKEN;
        generalLetterService.validateEncryptionOnUploadedDocuments(caseDocuments, caseId, auth);
        verify(bulkPrintDocumentService, times(1)).validateEncryptionOnUploadedDocument(
            doc1.getValue(), caseId, new ArrayList<>(), auth);
    }

    private List<DynamicRadioListElement> getDynamicRadioListItems(boolean addIntervenerListElements) {
        List<DynamicRadioListElement> listElements = new ArrayList<>(List.of(
            DynamicRadioListElement.builder().code(APPLICANT).label(APPLICANT).build(),
            DynamicRadioListElement.builder().code(APPLICANT_SOLICITOR).label(APP_SOLICITOR_LABEL).build(),
            DynamicRadioListElement.builder().code(RESPONDENT).label(RESPONDENT).build(),
            DynamicRadioListElement.builder().code(RESPONDENT_SOLICITOR).label(RESP_SOLICITOR_LABEL).build(),
            DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build()));
        if (addIntervenerListElements) {
            listElements.addAll(List.of(DynamicRadioListElement.builder().code(INTERVENER1).label(INTV1_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER1_SOLICITOR).label(INTV1_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER2).label(INTV2_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER2_SOLICITOR).label(INTV2_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER3).label(INTV3_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER3_SOLICITOR).label(INTV3_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER4).label(INTV4_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER4_SOLICITOR).label(INTV4_SOLICITOR_LABEL).build()
            ));
        }
        return listElements;
    }

    private Address getAddress() {
        return Address.builder().addressLine1("50 Regent Street").addressLine2("Second Line")
            .addressLine3("Third Line").county("Greater London")
            .postTown("London").postCode("W1B 5RL").build();
    }

    private void verifyAddress(Map<String, Object> data) {
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Regent Street\n"
            + "Second Line\n"
            + "Third Line\n"
            + "Greater London\n"
            + "London\n"
            + "W1B 5RL"));
    }

    private void verifyIntervenerName(Map<String, Object> data, String recipient) {
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is(recipient));
    }

    private void verifyIntervenerData(FinremCaseDetails caseDetails, DocumentCategory category, String intervenerName) {
        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));
        verifyDocumentFieldCategorisation(generalLetterData, category.getDocumentCategoryId());
        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        verifyAddress(data);
        verifyIntervenerName(data, intervenerName);
    }

    private void addIntervenerOneWrapper(FinremCaseData caseData) {
        IntervenerOneWrapper intervenerWrapper = IntervenerOneWrapper.builder().intervenerSolName("intvr1sol")
            .intervenerName("intvr1").intervenerAddress(getAddress()).build();
        caseData.setIntervenerOneWrapper(intervenerWrapper);
    }

    private void addIntervenerTwoWrapper(FinremCaseData caseData) {
        IntervenerTwoWrapper intervenerWrapper = IntervenerTwoWrapper.builder().intervenerSolName("intvr2sol")
            .intervenerName("intvr2").intervenerAddress(getAddress()).build();
        caseData.setIntervenerTwoWrapper(intervenerWrapper);
    }

    private void addIntervenerThreeWrapper(FinremCaseData caseData) {
        IntervenerThreeWrapper intervenerWrapper = IntervenerThreeWrapper.builder().intervenerSolName("intvr3sol")
            .intervenerName("intvr3").intervenerAddress(getAddress()).build();
        caseData.setIntervenerThreeWrapper(intervenerWrapper);
    }

    private void addIntervenerFourWrapper(FinremCaseData caseData) {
        IntervenerFourWrapper intervenerWrapper = IntervenerFourWrapper.builder().intervenerSolName("intvr4sol")
            .intervenerName("intvr4").intervenerAddress(getAddress()).build();
        caseData.setIntervenerFourWrapper(intervenerWrapper);
    }

    private static void verifyDocumentFields(CaseDocument result, String category, CaseDocument expectedResult) {
        assertThat(result.getDocumentFilename(), is(expectedResult.getDocumentFilename()));
        assertThat(result.getDocumentUrl(), is(expectedResult.getDocumentUrl()));
        assertThat(result.getDocumentBinaryUrl(), is(expectedResult.getDocumentBinaryUrl()));
        assertThat(result.getCategoryId(), is(category));
    }

    private void verifyLitigantNames(Map<String, Object> data) {
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("Moj Resp"));
    }

    private DynamicRadioList getDynamicRadioList(String code, String label, boolean addIntervenerListElements) {
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(code).label(label).build();
        return DynamicRadioList.builder().listItems(
            getDynamicRadioListItems(addIntervenerListElements)).value(chosenOption).build();
    }

    private void checkLitigantGeneralLetterData(Map<String, Object> data, String addressFirstLine) {
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is(addressFirstLine
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        verifyLitigantNames(data);
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
    }

    private void verifyCaseServiceCalls() {
        verify(caseDataService).buildFullApplicantName((CaseDetails) any());
        verify(caseDataService).buildFullRespondentName((CaseDetails) any());
    }

    private static void verifyDocumentFieldCategorisation(List<GeneralLetterCollection> generalLetterData, String CORRESPONDENCE_APPLICANT) {
        verifyDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(),
            CORRESPONDENCE_APPLICANT, caseDocument());
        verifyDocumentFields(generalLetterData.get(1).getValue().getGeneratedLetter(),
            CORRESPONDENCE_APPLICANT, caseDocument());
    }

    private FinremCaseDetails getCaseDetailsWithGeneralLetterData(String path) {
        return TestSetUpUtils.finremCaseDetailsFromResource(path, mapper);
    }

    private void addIntervenerWrapper(String intervenerCode) {
        FinremCaseData data = generalLetterContestedCaseDetails.getData();
        switch (intervenerCode) {
            case INTERVENER1, INTERVENER1_SOLICITOR:
                addIntervenerOneWrapper(data);
                break;
            case INTERVENER2, INTERVENER2_SOLICITOR:
                addIntervenerTwoWrapper(data);
                break;
            case INTERVENER3, INTERVENER3_SOLICITOR:
                addIntervenerThreeWrapper(data);
                break;
            case INTERVENER4, INTERVENER4_SOLICITOR:
                addIntervenerFourWrapper(data);
                break;
            default:
                throw new IllegalArgumentException("Invalid intervener code");
        }
    }

    private void testGenerateGeneralLetterForLitigant(String role, String addressFirstLine) {
        generalLetterCaseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_PATH);
        DynamicRadioList addresseeList = getDynamicRadioList(role, role, false);
        generalLetterCaseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, generalLetterCaseDetails);
        List<GeneralLetterCollection> generalLetterData = generalLetterCaseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));
        verifyDocumentFieldCategorisation(generalLetterData, null);
        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        checkLitigantGeneralLetterData(data, addressFirstLine);
        verifyCaseServiceCalls();
    }

    private void testGenerateGeneralLetterForIntervener(String intervenerCode, String intervenerLabel, DocumentCategory category, String intervenerName) {
        generalLetterContestedCaseDetails = getCaseDetailsWithGeneralLetterData(GENERAL_LETTER_CONTESTED_PATH);
        generalLetterContestedCaseDetails.getData().setCcdCaseType(CaseType.CONTESTED);
        addIntervenerWrapper(intervenerCode);
        DynamicRadioList addresseeList = getDynamicRadioList(intervenerCode, intervenerLabel, true);
        generalLetterContestedCaseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, generalLetterContestedCaseDetails);
        verifyIntervenerData(generalLetterContestedCaseDetails, category, intervenerName);
    }
}
