package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented.ConsentInContestMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented.MiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested.ContestedMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class OnlineFormDocumentServiceTest extends BaseServiceTest {

    public static final String TEST_URL = "http://test.url";
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private MiniFormADetailsMapper miniFormADetailsMapperMock;
    @MockBean
    private ContestedMiniFormADetailsMapper contestedMiniFormADetailsMapperMock;
    @MockBean
    private ConsentInContestMiniFormADetailsMapper consentInContestMiniFormADetailsMapperMock;

    @Autowired
    private DocumentConfiguration documentConfiguration;
    @Autowired
    private OnlineFormDocumentService onlineFormDocumentService;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void generateMiniFormA() {
        when(miniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(new HashMap<>());
        assertCaseDocument(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, emptyCaseDetails()));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(),
            eq(documentConfiguration.getMiniFormTemplate()), eq(documentConfiguration.getMiniFormFileName()));
    }

    @Test
    public void generateContestedMiniFormA() {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(contestedMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniFormA(AUTH_TOKEN, emptyCaseDetails()));
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getContestedMiniFormTemplate()), eq(documentConfiguration.getContestedMiniFormFileName()));
    }

    @Test
    public void generateConsentedInContestedMiniFormA() throws Exception {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(consentInContestMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);
        assertCaseDocument(onlineFormDocumentService.generateConsentedInContestedMiniFormA(consentedInContestedCaseDetails(), AUTH_TOKEN));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getMiniFormTemplate()), eq(documentConfiguration.getMiniFormFileName()));
    }

    @Test
    public void generateContestedDraftMiniFormA() {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(contestedMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);

        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(AUTH_TOKEN,
            FinremCaseDetails.builder().caseData(caseData()).build()));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getContestedDraftMiniFormTemplate()),
            eq(documentConfiguration.getContestedDraftMiniFormFileName()));

        verify(genericDocumentService, timeout(100).times(1))
            .deleteDocument(eq(TEST_URL), eq(AUTH_TOKEN));
    }

    @Test
    public void generateContestedDraftMiniFormA_NoOldMiniFormA() {
        Map<String, Object> placeholdersMap = new HashMap<>();
        when(contestedMiniFormADetailsMapperMock.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(placeholdersMap);
        assertCaseDocument(onlineFormDocumentService.generateDraftContestedMiniFormA(AUTH_TOKEN, emptyCaseDetails()));

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), eq(placeholdersMap),
            eq(documentConfiguration.getContestedDraftMiniFormTemplate()),
            eq(documentConfiguration.getContestedDraftMiniFormFileName()));

        verify(genericDocumentService, never()).deleteDocument(any(), any());
    }

    private FinremCaseData caseData() {
        FinremCaseData data = new FinremCaseData();
        data.setMiniFormA(Document.builder().url(TEST_URL).build());

        return data;
    }

    private FinremCaseDetails consentedInContestedCaseDetails() throws Exception {
        return finremCaseDetailsFromResource(getResource("/fixtures/mini-form-a-consent-in-contested.json"), mapper);
    }

    private FinremCaseDetails emptyCaseDetails() {
        return FinremCaseDetails.builder().caseData(new FinremCaseData()).build();
    }
}