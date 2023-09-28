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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

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
    private static final String INTV1_LABEL = "Intervener 1";
    private static final String INTV2_LABEL = "Intervener 2";
    private static final String INTV3_LABEL = "Intervener 3";
    private static final String INTV4_LABEL = "Intervener 4";
    private static final String INTV1_SOLICITOR_LABEL = "Intervener 1 Solicitor";
    private static final String INTV2_SOLICITOR_LABEL = "Intervener 2 Solicitor";
    private static final String INTV3_SOLICITOR_LABEL = "Intervener 3 Solicitor";
    private static final String INTV4_SOLICITOR_LABEL = "Intervener 4 Solicitor";
    private static final String RESP_SOLICITOR_LABEL = "Respondent Solicitor";
    private static final String RESP_LABEL = "Respondent";
    private static final String APP_LABEL = "Applicant";


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

    @Captor
    ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateGeneralLetterForApplicantforGivenConsentedCase() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(APPLICANT).label(APP_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        when(caseDataService.buildFullApplicantName(any())).thenReturn("Tom Geme");
        when(caseDataService.buildFullRespondentName(any())).thenReturn("Moj Resp");

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
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Applicant Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(data.get("applicantFullName"), is("Tom Geme"));
        assertThat(data.get("respondentFullName"), is("Moj Resp"));
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
        verify(caseDataService).buildFullApplicantName(any());
        verify(caseDataService).buildFullRespondentName(any());
    }


    @Test
    public void generateGeneralLetterForResponsentforGivenConsentedCase() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(RESPONDENT).label(RESP_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        when(caseDataService.buildFullApplicantName(any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(any())).thenReturn("Moj Resp");
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
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Respondent Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(data.get("applicantFullName"), is("Poor Guy"));
        assertThat(data.get("respondentFullName"), is("Moj Resp"));
        assertThat(data.get("generalLetterCreatedDate"), is(formattedNowDate));
        verify(caseDataService).buildFullApplicantName(any());
        verify(caseDataService).buildFullRespondentName(any());
    }

    @Test
    public void generateGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(RESPONDENT_SOLICITOR).label(RESP_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        when(caseDataService.buildFullApplicantName(any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(any())).thenReturn("test Korivi");

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
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Respondent Solicitor Street\n"
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
        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(APPLICANT_SOLICITOR).label(APP_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);

        when(caseDataService.buildFullApplicantName(any())).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(any())).thenReturn("Moj Resp");

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
        assertThat(data.get("respondentFullName"), is("Moj Resp"));
    }

    @Test
    public void generateContestedIntervenerOneGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerOneWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(INTERVENER1).label(INTV1_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener1 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr1"));
    }

    @Test
    public void generateContestedIntervenerOneSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerOneWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder()
            .code(INTERVENER1_SOLICITOR).label(INTV1_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder()
            .listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener1 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr1sol"));
    }

    @Test
    public void generateContestedIntervenerTwoSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerTwoWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder()
            .code(INTERVENER2_SOLICITOR).label(INTV2_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder()
            .listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener2 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr2sol"));
    }

    @Test
    public void generateContestedIntervenerTwoGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerTwoWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(INTERVENER2).label(INTV2_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener2 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr2"));
    }

    @Test
    public void generateContestedIntervenerThreeSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerThreeWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder()
            .code(INTERVENER3_SOLICITOR).label(INTV3_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(
            getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener3 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr3sol"));
    }

    @Test
    public void generateContestedIntervenerThreeGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerThreeWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(INTERVENER3).label(INTV3_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener3 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr3"));
    }

    @Test
    public void generateContestedIntervenerFourSolicitorGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerFourWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder()
            .code(INTERVENER4_SOLICITOR).label(INTV4_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder()
            .listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener4 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr4sol"));
    }

    @Test
    public void generateContestedIntervenerFourGeneralLetter() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/contested/general-letter-contested.json", mapper);
        FinremCaseData caseData = caseDetails.getData();
        addIntervenerFourWrapper(caseData);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(INTERVENER4).label(INTV4_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(true)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);

        List<GeneralLetterCollection> generalLetterData = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterCollection();
        assertThat(generalLetterData, hasSize(2));

        doCaseDocumentAssert(generalLetterData.get(0).getValue().getGeneratedLetter());
        doCaseDocumentAssert(generalLetterData.get(1).getValue().getGeneratedLetter());

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();
        assertThat(((Addressee) data.get(ADDRESSEE)).getFormattedAddress(), is("50 Intervener4 Solicitor Street\n"
            + "Second Address Line\n"
            + "Third Address Line\n"
            + "Greater London\n"
            + "London\n"
            + "SE12 9SE"));
        assertThat(((Addressee) data.get(ADDRESSEE)).getName(), is("intvr4"));
    }


    @Test
    public void givenNoPreviousGeneralLettersGenerated_generateGeneralLetter() throws Exception {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter-empty-collection.json", mapper);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(APPLICANT_SOLICITOR).label(APP_SOLICITOR_LABEL).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
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
    public void whenGeneralLetterPreviewCalled_thenPreviewDocumentIsAddedToCaseData() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);

        assertNull(caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());

        FinremCaseData caseData = caseDetails.getData();
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseData.getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.previewGeneralLetter(AUTH_TOKEN, caseDetails);
        assertNotNull(caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
    }

    @Test
    public void givenAddressIsMissing_whenCaseDataErrorsFetched_ThereIsAnError() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter-missing-address.json", mapper);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(RESPONDENT).label(RESPONDENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, hasItem("Address is missing for recipient type " + RESPONDENT));
    }

    @Test
    public void givenAddressIsPresent_whenCaseDataErrorsFetched_ThereIsNoError() {
        Address address = Address.builder().addressLine1("50 Other Street").addressLine2("Second Address Line")
            .addressLine3("Third Address Line").county("Greater London")
            .postTown("London").postCode("SE12 9SE").build();
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterRecipientAddress(address);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        List<String> errors = generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails);
        assertThat(errors, is(empty()));
    }

    @Test
    public void whenGeneralLetterIsCreated_thenItGetsSentToBulkPrint() {
        FinremCaseDetails caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/general-letter.json", mapper);
        DynamicRadioListElement chosenOption = DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build();
        DynamicRadioList addresseeList = DynamicRadioList.builder().listItems(getDynamicRadioListItems(false)).value(chosenOption).build();
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterAddressee(addresseeList);
        generalLetterService.createGeneralLetter(AUTH_TOKEN, caseDetails);
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

    private void addIntervenerOneWrapper(FinremCaseData caseData) {
        Address address = Address.builder().addressLine1("50 Intervener1 Solicitor Street").addressLine2("Second Address Line")
            .addressLine3("Third Address Line").county("Greater London")
            .postTown("London").postCode("SE12 9SE").build();
        IntervenerOneWrapper intervenerWrapper = IntervenerOneWrapper.builder().intervenerSolName("intvr1sol")
            .intervenerName("intvr1").intervenerAddress(address).build();
        caseData.setIntervenerOneWrapper(intervenerWrapper);
    }

    private void addIntervenerTwoWrapper(FinremCaseData caseData) {
        Address address = Address.builder().addressLine1("50 Intervener2 Solicitor Street").addressLine2("Second Address Line")
            .addressLine3("Third Address Line").county("Greater London")
            .postTown("London").postCode("SE12 9SE").build();
        IntervenerTwoWrapper intervenerWrapper = IntervenerTwoWrapper.builder().intervenerSolName("intvr2sol")
            .intervenerName("intvr2").intervenerAddress(address).build();
        caseData.setIntervenerTwoWrapper(intervenerWrapper);
    }

    private void addIntervenerThreeWrapper(FinremCaseData caseData) {
        Address address = Address.builder().addressLine1("50 Intervener3 Solicitor Street").addressLine2("Second Address Line")
            .addressLine3("Third Address Line").county("Greater London")
            .postTown("London").postCode("SE12 9SE").build();
        IntervenerThreeWrapper intervenerWrapper = IntervenerThreeWrapper.builder().intervenerSolName("intvr3sol")
            .intervenerName("intvr3").intervenerAddress(address).build();
        caseData.setIntervenerThreeWrapper(intervenerWrapper);
    }

    private void addIntervenerFourWrapper(FinremCaseData caseData) {
        Address address = Address.builder().addressLine1("50 Intervener4 Solicitor Street").addressLine2("Second Address Line")
            .addressLine3("Third Address Line").county("Greater London")
            .postTown("London").postCode("SE12 9SE").build();
        IntervenerFourWrapper intervenerWrapper = IntervenerFourWrapper.builder().intervenerSolName("intvr4sol")
            .intervenerName("intvr4").intervenerAddress(address).build();
        caseData.setIntervenerFourWrapper(intervenerWrapper);
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}
