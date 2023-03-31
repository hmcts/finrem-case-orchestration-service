package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested.ContestedMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_AUTHORISATION_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_3A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_3B;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;

public class OnlineFormDocumentServiceTest extends BaseServiceTest {

    private ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private GenericDocumentService genericDocumentService;

    @MockBean
    private ContestedMiniFormADetailsMapper contestedMiniFormADetailsMapperMock;

    @Autowired
    private DocumentConfiguration documentConfiguration;
    @Autowired
    private OnlineFormDocumentService onlineFormDocumentService;
    @Autowired
    private OptionIdToValueTranslator translator;

    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateMiniFormA() {
        assertCaseDocument(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));

        verify(genericDocumentService).generateDocument(AUTH_TOKEN, CaseDetails.builder().build(),
            documentConfiguration.getMiniFormTemplate(CaseDetails.builder().build()),
            documentConfiguration.getMiniFormFileName());
    }

    @Test
    public void generateMiniFormADirectFromMapWhenTypeOfApplicationNotPresentThenUseDefault() {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(contestedMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);
        FinremCaseDetails finremCaseDetails = emptyCaseDetails();
        finremCaseDetails.getData().setScheduleOneWrapper(ScheduleOneWrapper.builder()
            .typeOfApplication(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989).build());
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniForm(AUTH_TOKEN,
            emptyCaseDetails()));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getContestedMiniFormTemplate(emptyCaseDetails())),
            eq(documentConfiguration.getContestedMiniFormFileName()));
    }

    @Test
    public void generateMiniFormADirectFromMapWhenTypeOfApplicationPresentAndSchedule1ThenUseChooseTemplate() {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(contestedMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);
        FinremCaseDetails finremCaseDetails = emptyCaseDetails();
        finremCaseDetails.getData().setScheduleOneWrapper(ScheduleOneWrapper.builder()
            .typeOfApplication(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989).build());
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniForm(AUTH_TOKEN, finremCaseDetails));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getContestedMiniFormScheduleTemplate(finremCaseDetails)),
            eq(documentConfiguration.getContestedMiniFormFileName()));
    }

    @Test
    public void generateMiniFormADirectFromMapWhenTypeOfApplicationPresentAndNotSchedule1ThenUseChooseTemplate() {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(contestedMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);
        FinremCaseDetails finremCaseDetails = emptyCaseDetails();
        finremCaseDetails.getData().setScheduleOneWrapper(ScheduleOneWrapper.builder()
            .typeOfApplication(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS).build());
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniForm(AUTH_TOKEN, finremCaseDetails));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getContestedMiniFormTemplate(finremCaseDetails)),
            eq(documentConfiguration.getContestedMiniFormFileName()));
    }

    @Test
    public void generateContestedMiniFormA() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniFormA(AUTH_TOKEN, caseDetails));
        verify(genericDocumentService).generateDocument(AUTH_TOKEN, CaseDetails.builder().build(),
            documentConfiguration.getContestedMiniFormTemplate(caseDetails), documentConfiguration.getContestedMiniFormFileName());
    }

    @Test
    public void generateConsentedInContestedMiniFormA() throws Exception {
        String payload = "/fixtures/mini-form-a-consent-in-contested.json";
        assertCaseDocument(onlineFormDocumentService
            .generateConsentedInContestedMiniFormA(consentedInContestedCaseDetails(payload), AUTH_TOKEN));

        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getMiniFormTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getMiniFormFileName()));

        verifyAdditionalFields(caseDetailsArgumentCaptor.getValue().getData());
    }

    @Test
    public void generateConsentedInContestedMiniFormASchedule1() throws Exception {
        String payload = "/fixtures/mini-form-a-consent-in-contested-schedule1.json";
        assertCaseDocument(onlineFormDocumentService
            .generateConsentedInContestedMiniFormA(consentedInContestedCaseDetails(payload), AUTH_TOKEN));

        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getMiniFormTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getMiniFormFileName()));

        verifyAdditionalFields(caseDetailsArgumentCaptor.getValue().getData());
    }

    @Test
    public void generateContestedDraftMiniFormA() {
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().data(caseData()).build()));

        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedDraftMiniFormTemplate()), eq(documentConfiguration.getContestedDraftMiniFormFileName()));
    }

    @Test
    public void generateContestedDraftMiniFormASchedule() {
        Map<String, Object> data = caseData();
        data.put(TYPE_OF_APPLICATION, "Under paragraph 1 or 2 of schedule 1 children act 1989");
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().data(data).build()));

        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getContestedDraftMiniFormTemplateSchedule()), eq(documentConfiguration.getContestedDraftMiniFormFileName()));
    }

    private Map<String, Object> caseData() {
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put("document_url", "http://test.url");

        Map<String, Object> data = new HashMap<>();
        data.put(MINI_FORM_A, documentMap);
        data.put(TYPE_OF_APPLICATION, "In connection to matrimonial and civil partnership proceedings");
        return data;
    }

    private CaseDetails consentedInContestedCaseDetails(String payload) throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(payload)) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private void verifyAdditionalFields(Map<String, Object> data) {
        //Solicitor Details
        assertThat(data.get(CONSENTED_SOLICITOR_NAME), is("Solicitor"));
        assertThat(data.get(CONSENTED_SOLICITOR_FIRM), is("Awesome Firm"));

        assertThat(data, IsMapContaining.hasKey(CONSENTED_SOLICITOR_ADDRESS));
        Map<String, Object> addressObject = (Map<String, Object>) data.get(CONSENTED_SOLICITOR_ADDRESS);
        assertThat(addressObject.get("County").toString(), is("County"));
        assertThat(addressObject.get("Country").toString(), is("UK"));
        assertThat(addressObject.get("PostCode").toString(), is("SW1A 1AA"));
        assertThat(addressObject.get("PostTown").toString(), is("London"));
        assertThat(addressObject.get("AddressLine1").toString(), is("Buckingham Palace"));
        assertThat(addressObject.get("AddressLine2").toString(), is("null"));
        assertNull(addressObject.get("AddressLine3"));

        //Respondent Details
        assertThat(data.get(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME), is("john"));
        assertThat(data.get(CONSENTED_RESPONDENT_LAST_NAME), is("smith"));
        assertThat(data.get(CONSENTED_RESPONDENT_REPRESENTED), is("No"));

        //Checklist
        assertThat(data, IsMapContaining.hasKey(CONSENTED_NATURE_OF_APPLICATION));
        assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(0).toString(), is("Periodical Payment Order"));
        assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(1).toString(), is("Lump Sum Order"));
        assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(2).toString(), is("Property Adjustment Order"));

        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_3A), is("test"));
        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_3B), is("test"));

        //Order For Children Reasons
        assertThat(data.get(CONSENTED_ORDER_FOR_CHILDREN), is("Yes"));
        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_5), is("No"));

        assertThat(data, IsMapContaining.hasKey(CONSENTED_NATURE_OF_APPLICATION_6));
        assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION_6)).get(0).toString(), is("item1"));
        assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION_6)).get(1).toString(), is("item2"));

        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_7), is("test"));

        assertThat(data.get(CONSENTED_AUTHORISATION_FIRM), is("Authorised Firm"));
    }


    private FinremCaseDetails emptyCaseDetails() {
        return FinremCaseDetails.builder().data(new FinremCaseData()).build();
    }
}