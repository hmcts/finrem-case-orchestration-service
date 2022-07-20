package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamDomesticViolence;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class ContestedOnlineFormTest extends GenerateMiniFormATest {

    @Override
    protected String apiUrl() {
        return "/case-orchestration/documents/generate-contested-mini-form-a";
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/generate-contested-form-A.json";
    }

    @Override
    void setUpMockContext() {
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getContestedMiniFormTemplate()),
            eq(documentConfiguration.getContestedMiniFormFileName())))
            .thenReturn(newDocument());
    }

    @Override
    protected void assertPlaceholdersMap() {
        Map<String, Object> caseDetailsMap = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> placeholdersMap = (Map<String, Object>) caseDetailsMap.get(CASE_DATA);
        assertEquals(NO_VALUE, placeholdersMap.get("respondentRepresented"));
        assertEquals(NO_VALUE, placeholdersMap.get("familyMediatorMIAM"));
        assertEquals(YES_VALUE, placeholdersMap.get("applicantRepresented"));
        assertEquals("test", placeholdersMap.get("natureOfApplication7"));
        assertEquals(NO_VALUE, placeholdersMap.get("applicantAttendedMIAM"));
        assertEquals("test", placeholdersMap.get("authorisationName"));
        assertEquals("Poor", placeholdersMap.get("applicantFMName"));
        assertEquals(YES_VALUE, placeholdersMap.get("fastTrackDecision"));
        assertEquals("9963472494", placeholdersMap.get("respondentPhone"));
        assertEquals("2010-01-01", placeholdersMap.get("authorisation3"));
        assertEquals(NO_VALUE, placeholdersMap.get("applicantAddressConfidential"));
        assertEquals("2019-03-04", placeholdersMap.get("issueDate"));
        assertEquals(NO_VALUE, placeholdersMap.get("claimingExemptionMIAM"));
        assertEquals("Guy", placeholdersMap.get("applicantLName"));
        assertEquals(NO_VALUE, placeholdersMap.get("respondentAddressConfidential"));
        assertTrue(((List<String>) placeholdersMap.get("MIAMDomesticViolenceChecklist"))
            .containsAll(miamDomesticeViolenceChecklist()));

    }

    private List<String> miamDomesticeViolenceChecklist() {
        return List.of(
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_2.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_3.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_4.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_5.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_6.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_7.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_8.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_11.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_13.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_15.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_18.getText(),
            MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_19.getText()
            );
    }

    @Override
    protected void verifyDocumentServiceInteraction() {
        verify(genericDocumentServiceMock, times(1))
            .generateDocumentFromPlaceholdersMap(any(),
                placeholdersMapCaptor.capture(),
                eq(documentConfiguration.getContestedMiniFormTemplate()),
                eq(documentConfiguration.getContestedMiniFormFileName()));
    }

    @Override
    protected void setUp500MockContext() {
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getContestedMiniFormTemplate()),
            eq(documentConfiguration.getContestedMiniFormFileName()))).thenThrow(feignError());
    }
}
