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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
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

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    private FinremCaseDetails getCaseDetailsWithGeneralLetterData(String path) {
        return TestSetUpUtils.finremCaseDetailsFromResource(path, mapper);
    }

    @Test
    public void generateGeneralLetterForApplicantforGivenConsentedCase() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter.json");
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioList addresseeList = getDynamicRadioList(APPLICANT, APPLICANT, false);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn("Moj Resp");

        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(), null);
        verifyCaseDocumentFields(generalLetterData.get(1).getValue().getGeneratedLetter(), null);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Applicant Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        verifyLitigantNames(data);
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
        verify(caseDataService).buildFullApplicantName((CaseDetails) any());
        verify(caseDataService).buildFullRespondentName((CaseDetails) any());
    }

    @Test
    public void generateGeneralLetterForResponsentforGivenConsentedCase() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter.json");
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioList addresseeList = getDynamicRadioList(RESPONDENT, RESPONDENT, false);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn("Moj Resp");
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(), null);
        verifyCaseDocumentFields(generalLetterData.get(1).getValue().getGeneratedLetter(), null);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Respondent Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        verifyLitigantNames(data);
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
        verify(caseDataService).buildFullApplicantName((CaseDetails) any());
        verify(caseDataService).buildFullRespondentName((CaseDetails) any());
    }

    @Test
    public void generateGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter.json");
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioList addresseeList = getDynamicRadioList(RESPONDENT_SOLICITOR, RESP_SOLICITOR_LABEL, false);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn("Moj Resp");

        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(), null);
        verifyCaseDocumentFields(generalLetterData.get(1).getValue().getGeneratedLetter(), null);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Respondent Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        verifyLitigantNames(data);
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
    }

    @Test
    public void generateContestedGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        DynamicRadioList addresseeList = getDynamicRadioList(APPLICANT_SOLICITOR, APP_SOLICITOR_LABEL, false);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);

        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn("Moj Resp");

        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(),
            DocumentCategory.CORRESPONDENCE_APPLICANT.getDocumentCategoryId());
        verifyCaseDocumentFields(generalLetterData.get(1).getValue().getGeneratedLetter(),
            DocumentCategory.CORRESPONDENCE_APPLICANT.getDocumentCategoryId());

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
        verifyLitigantNames(data);
    }

    @Test
    public void generateContestedIntervenerOneGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerOneWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER1, INTV1_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_1, "intvr1");
    }

    @Test
    public void generateContestedIntervenerOneSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerOneWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER1_SOLICITOR, INTV1_SOLICITOR_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_1, "intvr1sol");
    }

    @Test
    public void generateContestedIntervenerTwoSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerTwoWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER2_SOLICITOR, INTV2_SOLICITOR_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_2, "intvr2sol");
    }

    @Test
    public void generateContestedIntervenerTwoGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerTwoWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER2, INTV2_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_2, "intvr2");
    }

    @Test
    public void generateContestedIntervenerThreeSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerThreeWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER3_SOLICITOR, INTV3_SOLICITOR_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_3, "intvr3sol");
    }

    @Test
    public void generateContestedIntervenerThreeGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerThreeWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER3, INTV3_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_3, "intvr3");
    }

    @Test
    public void generateContestedIntervenerFourSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerFourWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER4_SOLICITOR, INTV4_SOLICITOR_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_4, "intvr4sol");
    }

    @Test
    public void generateContestedIntervenerFourGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/contested/general-letter-contested.json");
        FinremCaseData caseData = caseDetails.getData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        addIntervenerFourWrapper(caseData);
        DynamicRadioList addresseeList = getDynamicRadioList(INTERVENER4, INTV4_LABEL, true);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        verifyIntervenerData(caseDetails, DocumentCategory.CORRESPONDENCE_INTERVENER_4, "intvr4");
    }

    private void verifyIntervenerData(FinremCaseDetails caseDetails, DocumentCategory category, String intervenerName) {
        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));
        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(), category.getDocumentCategoryId());
        verifyCaseDocumentFields(generalLetterData.get(1).getValue().getGeneratedLetter(), category.getDocumentCategoryId());
        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        verifyAddress(data);
        verifyIntervenerName(data, intervenerName);
    }

    @Test
    public void givenNoPreviousGeneralLettersGenerated_generateGeneralLetter() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter-empty-collection.json");
        DynamicRadioList addresseeList = getDynamicRadioList(APPLICANT_SOLICITOR, APP_SOLICITOR_LABEL, false);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterUploadedDocument(caseDocument());
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();

        assertThat(generalLetterData, hasSize(1));
        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(), null);
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument(), AUTH_TOKEN, String.valueOf(caseDetails.getId()));
        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());
        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
        assertThat(data.get("ccdCaseNumber"), is(1234567891L));
    }

    @Test
    public void whenGeneralLetterPreviewCalled_thenPreviewDocumentIsAddedToCaseData() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter.json");
        assertNull(caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioList addresseeList = getDynamicRadioList(OTHER_RECIPIENT, OTHER_RECIPIENT, false);
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.previewGeneralLetter(AUTH_TOKEN, caseDetails);
        assertNotNull(caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
    }

    @Test
    public void givenAddressIsMissing_whenCaseDataErrorsFetched_ThereIsAnError() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter-missing-address.json");
        DynamicRadioList addresseeList = getDynamicRadioList(RESPONDENT, RESPONDENT, true);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, hasItem("Address is missing for recipient type " + RESPONDENT));
    }

    @Test
    public void givenAddressIsPresent_whenCaseDataErrorsFetched_ThereIsNoError() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter.json");
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterRecipientAddress(getAddress());
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, is(empty()));
    }

    @Test
    public void whenGeneralLetterIsCreated_thenItGetsSentToBulkPrint() {
        FinremCaseDetails caseDetails = getCaseDetailsWithGeneralLetterData("/fixtures/general-letter.json");
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        caseData.setCcdCaseType(CaseType.CONTESTED);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        verifyCaseDocumentFields(generalLetterData.get(0).getValue().getGeneratedLetter(),
            DocumentCategory.CORRESPONDENCE_OTHER.getDocumentCategoryId());
        verify(bulkPrintService, times(1)).bulkPrintFinancialRemedyLetterPack(anyLong(), any(), any(), any());
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

    private void addIntervenerOneWrapper(FinremCaseData caseData) {
        IntervenerOne intervenerWrapper = IntervenerOne.builder().intervenerSolName("intvr1sol")
            .intervenerName("intvr1").intervenerAddress(getAddress()).build();
        caseData.setIntervenerOne(intervenerWrapper);
    }

    private void addIntervenerTwoWrapper(FinremCaseData caseData) {
        IntervenerTwo intervenerWrapper = IntervenerTwo.builder().intervenerSolName("intvr2sol")
            .intervenerName("intvr2").intervenerAddress(getAddress()).build();
        caseData.setIntervenerTwo(intervenerWrapper);
    }

    private void addIntervenerThreeWrapper(FinremCaseData caseData) {
        IntervenerThree intervenerWrapper = IntervenerThree.builder().intervenerSolName("intvr3sol")
            .intervenerName("intvr3").intervenerAddress(getAddress()).build();
        caseData.setIntervenerThree(intervenerWrapper);
    }

    private void addIntervenerFourWrapper(FinremCaseData caseData) {
        IntervenerFour intervenerWrapper = IntervenerFour.builder().intervenerSolName("intvr4sol")
            .intervenerName("intvr4").intervenerAddress(getAddress()).build();
        caseData.setIntervenerFour(intervenerWrapper);
    }

    private static void verifyCaseDocumentFields(CaseDocument result, String category) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
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
}
