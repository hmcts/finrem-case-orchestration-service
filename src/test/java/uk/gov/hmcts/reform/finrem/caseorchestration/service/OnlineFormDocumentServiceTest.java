package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.nio.file.Files;
import java.nio.file.Paths;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
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

public class OnlineFormDocumentServiceTest extends BaseServiceTest {

    private ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Autowired
    private DocumentConfiguration documentConfiguration;
    @Autowired
    private OnlineFormDocumentService onlineFormDocumentService;
    @Autowired
    private OptionIdToValueTranslator translator;
    @Autowired
    private FinremCallbackRequestDeserializer deserializer;

    @Captor
    private ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void generateMiniFormA() {
        assertCaseDocument(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, FinremCaseDetails.builder().build()));

        verify(genericDocumentService).generateDocument(AUTH_TOKEN, CaseDetails.builder().build(),
            documentConfiguration.getMiniFormTemplate(), documentConfiguration.getMiniFormFileName());
    }

    @Test
    public void generateContestedMiniFormA() {
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniFormA(AUTH_TOKEN, FinremCaseDetails.builder().build()));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, new HashMap(),
            documentConfiguration.getContestedMiniFormTemplate(), documentConfiguration.getContestedMiniFormFileName());
    }

    @Test
    public void generateConsentedInContestedMiniFormA() throws Exception {
        assertCaseDocument(onlineFormDocumentService.generateConsentedInContestedMiniFormA(consentedInContestedCaseDetails(), AUTH_TOKEN));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(documentConfiguration.getMiniFormTemplate()), eq(documentConfiguration.getMiniFormFileName()));

        verifyAdditionalFields(placeholdersMapCaptor.getValue());
    }

    @Test
    public void generateContestedDraftMiniFormA() {
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(AUTH_TOKEN,
            FinremCaseDetails.builder().caseData(caseData()).build()));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(documentConfiguration.getContestedDraftMiniFormTemplate()),
            eq(documentConfiguration.getContestedDraftMiniFormFileName()));
    }

    private FinremCaseData caseData() {
        FinremCaseData data = new FinremCaseData();
        data.setMiniFormA(Document.builder().url("http://test.url").build());

        return data;
    }

    private FinremCaseDetails consentedInContestedCaseDetails() throws Exception {
        return deserializer.deserialize(new String(Files.readAllBytes(Paths.get("/fixtures/mini-form-a-consent-in-contested.json"))))
            .getCaseDetails();
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
}