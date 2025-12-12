package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested.ContestedMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypeOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerServiceTest.CASE_ID;

@ExtendWith(MockitoExtension.class)
class OnlineFormDocumentServiceTest {

    @Mock
    private ContestedMiniFormADetailsMapper contestedMiniFormADetailsMapper;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private DocumentHelper documentHelper;

    @Mock
    private ConsentedApplicationHelper consentedApplicationHelper;

    private OptionIdToValueTranslator optionIdToValueTranslator;

    @InjectMocks
    private OnlineFormDocumentService onlineFormDocumentService;

    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @BeforeEach
    void setUp() {
        optionIdToValueTranslator = spy(new OptionIdToValueTranslator("/options/options-id-value-transform.json",
            new ObjectMapper()));
        optionIdToValueTranslator.initOptionValueMap();
        ReflectionTestUtils.setField(onlineFormDocumentService, "optionIdToValueTranslator", optionIdToValueTranslator);
    }

    @Test
    void generateMiniFormA() {
        // Arrange
        CaseDetails caseDetailsCopy = mock(CaseDetails.class);
        when(documentHelper.deepCopy(any(CaseDetails.class), eq(CaseDetails.class))).thenReturn(caseDetailsCopy);

        CaseDetails providedCaseDetails = mock(CaseDetails.class);
        String template = "TEMPLATE";
        when(documentConfiguration.getMiniFormTemplate(providedCaseDetails)).thenReturn(template);
        String filename = "FILE_NAME";
        when(documentConfiguration.getMiniFormFileName()).thenReturn(filename);

        when(genericDocumentService.generateDocument(AUTH_TOKEN, caseDetailsCopy, "TEMPLATE", "FILE_NAME"))
            .thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, providedCaseDetails));
        verify(genericDocumentService).generateDocument(AUTH_TOKEN, caseDetailsCopy, "TEMPLATE", "FILE_NAME");
    }

    @Test
    void generateMiniFormADirectFromMapWhenTypeOfApplicationNotPresentThenUseDefault() {
        // Arrange
        DefaultCourtListWrapper defaultCourtListWrapper = mock(DefaultCourtListWrapper.class);

        RegionWrapper regionWrapper = mock(RegionWrapper.class);
        when(regionWrapper.getDefaultCourtList()).thenReturn(defaultCourtListWrapper);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getRegionWrapper()).thenReturn(regionWrapper);

        final FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(CASE_ID, CONTESTED, finremCaseData).build();

        String template = "TEMPLATE";
        when(documentConfiguration.getContestedMiniFormTemplate(finremCaseDetails)).thenReturn(template);
        String filename = "FILE_NAME";
        when(documentConfiguration.getContestedMiniFormFileName()).thenReturn(filename);
        Map<String, Object> defaultPlaceholdersMap = Map.of();
        when(contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails, defaultCourtListWrapper))
            .thenReturn(defaultPlaceholdersMap);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, defaultPlaceholdersMap,
            "TEMPLATE", "FILE_NAME", finremCaseDetails.getCaseType()))
            .thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniForm(AUTH_TOKEN, finremCaseDetails));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, defaultPlaceholdersMap,
            "TEMPLATE", "FILE_NAME", CONTESTED);
    }

    @Test
    void generateMiniFormADirectFromMapWhenTypeOfApplicationPresentAndSchedule1ThenUseChooseTemplate() {
        // Arrange
        DefaultCourtListWrapper defaultCourtListWrapper = mock(DefaultCourtListWrapper.class);

        RegionWrapper regionWrapper = mock(RegionWrapper.class);
        when(regionWrapper.getDefaultCourtList()).thenReturn(defaultCourtListWrapper);

        ScheduleOneWrapper scheduleOneWrapper = ScheduleOneWrapper.builder()
            .typeOfApplication(SCHEDULE_1_CHILDREN_ACT_1989).build();

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getScheduleOneWrapper()).thenReturn(scheduleOneWrapper);
        when(finremCaseData.getRegionWrapper()).thenReturn(regionWrapper);

        final FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(CASE_ID, CONTESTED, finremCaseData).build();

        String template = "TEMPLATE";
        when(documentConfiguration.getContestedMiniFormScheduleTemplate(finremCaseDetails)).thenReturn(template);
        String filename = "FILE_NAME";
        when(documentConfiguration.getContestedMiniFormFileName()).thenReturn(filename);
        Map<String, Object> defaultPlaceholdersMap = Map.of("a", "a");
        when(contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails, defaultCourtListWrapper))
            .thenReturn(defaultPlaceholdersMap);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, defaultPlaceholdersMap,
            "TEMPLATE", "FILE_NAME", CONTESTED))
            .thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniForm(AUTH_TOKEN, finremCaseDetails));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, defaultPlaceholdersMap,
            "TEMPLATE", "FILE_NAME", CONTESTED);
    }

    @Test
    void generateMiniFormADirectFromMapWhenTypeOfApplicationPresentAndNotSchedule1ThenUseChooseTemplate() {
        // Arrange
        DefaultCourtListWrapper defaultCourtListWrapper = mock(DefaultCourtListWrapper.class);

        RegionWrapper regionWrapper = mock(RegionWrapper.class);
        when(regionWrapper.getDefaultCourtList()).thenReturn(defaultCourtListWrapper);

        ScheduleOneWrapper scheduleOneWrapper = ScheduleOneWrapper.builder()
            .typeOfApplication(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS).build();

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getScheduleOneWrapper()).thenReturn(scheduleOneWrapper);
        when(finremCaseData.getRegionWrapper()).thenReturn(regionWrapper);

        final FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(CASE_ID, CONTESTED, finremCaseData).build();

        String template = "TEMPLATE";
        when(documentConfiguration.getContestedMiniFormTemplate(finremCaseDetails)).thenReturn(template);
        String filename = "FILE_NAME";
        when(documentConfiguration.getContestedMiniFormFileName()).thenReturn(filename);
        Map<String, Object> defaultPlaceholdersMap = Map.of();
        when(contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails, defaultCourtListWrapper))
            .thenReturn(defaultPlaceholdersMap);

        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, defaultPlaceholdersMap,
            "TEMPLATE", "FILE_NAME", finremCaseDetails.getCaseType()))
            .thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniForm(AUTH_TOKEN, finremCaseDetails));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, defaultPlaceholdersMap,
            "TEMPLATE", "FILE_NAME", CONTESTED);
    }

    @Test
    void generateConsentedInContestedMiniFormA() {
        // Arrange
        String payload = "/fixtures/mini-form-a-consent-in-contested.json";
        CaseDetails caseDetails = consentedInContestedCaseDetails(payload);
        when(documentHelper.deepCopy(any(CaseDetails.class), eq(CaseDetails.class))).thenReturn(caseDetails);
        when(documentConfiguration.getMiniFormTemplate(caseDetails)).thenReturn("TEMPLATE");
        when(documentConfiguration.getMiniFormFileName()).thenReturn("FILE_NAME");
        when(genericDocumentService.generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq("TEMPLATE"), eq("FILE_NAME")))
            .thenReturn(caseDocument());

        // Act
        assertCaseDocument(onlineFormDocumentService
            .generateConsentedInContestedMiniFormA(caseDetails, AUTH_TOKEN));

        verify(genericDocumentService).generateDocument(AUTH_TOKEN, caseDetailsArgumentCaptor.getValue(), "TEMPLATE", "FILE_NAME");
        verifyAdditionalFields(caseDetailsArgumentCaptor.getValue().getData());
    }

    @Test
    void generateConsentedInContestedMiniFormASchedule1() {
        String payload = "/fixtures/mini-form-a-consent-in-contested-schedule1.json";

        CaseDetails caseDetails = consentedInContestedCaseDetails(payload);
        when(documentHelper.deepCopy(any(CaseDetails.class), eq(CaseDetails.class))).thenReturn(caseDetails);
        when(documentConfiguration.getMiniFormTemplate(caseDetails)).thenReturn("TEMPLATE");
        when(documentConfiguration.getMiniFormFileName()).thenReturn("FILE_NAME");
        when(genericDocumentService.generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq("TEMPLATE"), eq("FILE_NAME")))
            .thenReturn(caseDocument());

        assertCaseDocument(onlineFormDocumentService
            .generateConsentedInContestedMiniFormA(caseDetails, AUTH_TOKEN));

        verify(genericDocumentService).generateDocument(AUTH_TOKEN, caseDetailsArgumentCaptor.getValue(), "TEMPLATE", "FILE_NAME");
        verifyAdditionalFields(caseDetailsArgumentCaptor.getValue().getData());
    }

    @Test
    void generateContestedDraftMiniFormAEmptyFinRemCaseDetails() {
        DefaultCourtListWrapper defaultCourtListWrapper = mock(DefaultCourtListWrapper.class);

        RegionWrapper regionWrapper = mock(RegionWrapper.class);
        when(regionWrapper.getDefaultCourtList()).thenReturn(defaultCourtListWrapper);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getRegionWrapper()).thenReturn(regionWrapper);

        final FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(CASE_ID, CONTESTED, finremCaseData).build();

        when(documentConfiguration.getContestedDraftMiniFormFileName()).thenReturn("FILE_NAME");
        when(documentConfiguration.getContestedDraftMiniFormTemplate()).thenReturn("TEMPLATE");

        when(contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails, defaultCourtListWrapper))
            .thenReturn(Map.of("a", "a"));
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, Map.of("a", "a"),
            "TEMPLATE", "FILE_NAME", CONTESTED)).thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(
            AUTH_TOKEN, finremCaseDetails));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, Map.of("a", "a"),
            "TEMPLATE", "FILE_NAME", CONTESTED);
    }

    @Test
    void generateContestedDraftMiniFormAFinremFormAMatrimonial() {
        DefaultCourtListWrapper defaultCourtListWrapper = mock(DefaultCourtListWrapper.class);

        RegionWrapper regionWrapper = mock(RegionWrapper.class);
        when(regionWrapper.getDefaultCourtList()).thenReturn(defaultCourtListWrapper);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getRegionWrapper()).thenReturn(regionWrapper);
        when(finremCaseData.getScheduleOneWrapper())
            .thenReturn(ScheduleOneWrapper.builder().typeOfApplication(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS).build());

        final FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(CASE_ID, CONTESTED, finremCaseData).build();

        when(documentConfiguration.getContestedDraftMiniFormFileName()).thenReturn("FILE_NAME");
        when(documentConfiguration.getContestedDraftMiniFormTemplate()).thenReturn("TEMPLATE");

        when(contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails, defaultCourtListWrapper))
            .thenReturn(Map.of("a", "a"));
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(AUTH_TOKEN, Map.of("a", "a"),
            "TEMPLATE", "FILE_NAME", CONTESTED)).thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(
            AUTH_TOKEN, finremCaseDetails));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, Map.of("a", "a"),
            "TEMPLATE", "FILE_NAME", CONTESTED);
    }

    @Test
    void generateContestedDraftMiniFormASchedule1() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of(TYPE_OF_APPLICATION, TypeOfApplication.SCHEDULE_ONE)).build();

        CaseDetails copy = CaseDetails.builder().data(new HashMap<>()).build();
        when(documentHelper.deepCopy(caseDetails, CaseDetails.class)).thenReturn(copy);
        when(documentConfiguration.getContestedDraftMiniFormTemplateSchedule()).thenReturn("TEMPLATE");
        when(documentConfiguration.getContestedDraftMiniFormFileName()).thenReturn("FILE_NAME");
        when(genericDocumentService.generateDocument(AUTH_TOKEN, copy,"TEMPLATE", "FILE_NAME"))
            .thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(
            AUTH_TOKEN, caseDetails));

        verify(optionIdToValueTranslator).translateFixedListOptions(copy);
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq("TEMPLATE"), eq("FILE_NAME"));
    }

    @Test
    void generateContestedDraftMiniFormASchedule1AndDeleteOldMiniFormA() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of(
            TYPE_OF_APPLICATION, TypeOfApplication.SCHEDULE_ONE,
            MINI_FORM_A, Map.of("document_url","oldMiniFormADocumentUrl")
        )).build();

        CaseDetails copy = CaseDetails.builder().data(new HashMap<>()).build();
        when(documentHelper.deepCopy(caseDetails, CaseDetails.class)).thenReturn(copy);
        when(documentConfiguration.getContestedDraftMiniFormTemplateSchedule()).thenReturn("TEMPLATE");
        when(documentConfiguration.getContestedDraftMiniFormFileName()).thenReturn("FILE_NAME");
        when(genericDocumentService.generateDocument(AUTH_TOKEN, copy,"TEMPLATE", "FILE_NAME"))
            .thenReturn(caseDocument());

        // Act & Verify
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(
            AUTH_TOKEN, caseDetails));

        verify(optionIdToValueTranslator).translateFixedListOptions(copy);
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq("TEMPLATE"), eq("FILE_NAME"));

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(genericDocumentService)
                .deleteDocument("oldMiniFormADocumentUrl", AUTH_TOKEN));
    }

    private CaseDetails consentedInContestedCaseDetails(String payload) {
        try {
            try (InputStream resourceAsStream = getClass().getResourceAsStream(payload)) {
                return new ObjectMapper().readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Fail to load payload.", e);
        }
    }

    private static void verifyAdditionalFields(Map<String, Object> data) {
        //Solicitor Details
        assertThat(data.get(CONSENTED_SOLICITOR_NAME)).isEqualTo("Solicitor");
        assertThat(data.get(CONSENTED_SOLICITOR_FIRM)).isEqualTo("Awesome Firm");

        assertThat(data).containsKey(CONSENTED_SOLICITOR_ADDRESS);
        Map<String, Object> addressObject = convertToMap(data.get(CONSENTED_SOLICITOR_ADDRESS));

        assertThat(addressObject.get("County").toString()).isEqualTo("County");
        assertThat(addressObject.get("Country").toString()).isEqualTo("UK");
        assertThat(addressObject.get("PostCode").toString()).isEqualTo("SW1A 1AA");
        assertThat(addressObject.get("PostTown").toString()).isEqualTo("London");
        assertThat(addressObject.get("AddressLine1").toString()).isEqualTo("Buckingham Palace");
        assertThat(addressObject.get("AddressLine2").toString()).isEqualTo("null");
        assertNull(addressObject.get("AddressLine3"));

        //Respondent Details
        assertThat(data.get(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME)).isEqualTo("john");
        assertThat(data.get(CONSENTED_RESPONDENT_LAST_NAME)).isEqualTo("smith");
        assertThat(data.get(CONSENTED_RESPONDENT_REPRESENTED)).isEqualTo("No");

        //Checklist
        assertThat(data).containsKey(CONSENTED_NATURE_OF_APPLICATION);
        assertThat(((ArrayList<?>) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(0).toString()).isEqualTo("Periodical Payment Order");
        assertThat(((ArrayList<?>) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(1).toString()).isEqualTo("Lump Sum Order");
        assertThat(((ArrayList<?>) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(2).toString()).isEqualTo("Property Adjustment Order");

        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_3A)).isEqualTo("test");
        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_3B)).isEqualTo("test");

        //Order For Children Reasons
        assertThat(data.get(CONSENTED_ORDER_FOR_CHILDREN)).isEqualTo("Yes");
        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_5)).isEqualTo("No");

        assertThat(data).containsKey(CONSENTED_NATURE_OF_APPLICATION_6);
        assertThat(((ArrayList<?>) data.get(CONSENTED_NATURE_OF_APPLICATION_6)).get(0).toString()).isEqualTo("item1");
        assertThat(((ArrayList<?>) data.get(CONSENTED_NATURE_OF_APPLICATION_6)).get(1).toString()).isEqualTo("item2");

        assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_7)).isEqualTo("test");

        assertThat(data.get(CONSENTED_AUTHORISATION_FIRM)).isEqualTo("Authorised Firm");
    }

    protected static Map<String, Object> convertToMap(Object object) {
        return new ObjectMapper().convertValue(object, new TypeReference<>() {
        });
    }
}
