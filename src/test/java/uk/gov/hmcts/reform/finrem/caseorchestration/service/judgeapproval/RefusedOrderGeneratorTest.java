package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class RefusedOrderGeneratorTest {

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @InjectMocks
    private RefusedOrderGenerator underTest;

    @Test
    void shouldGenerateRefuseOrder() {
        // Arrange
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(12345L).build();
        FinremCaseData caseData = new FinremCaseData();
        DraftOrdersWrapper draftOrdersWrapper = new DraftOrdersWrapper();
        caseData.setDraftOrdersWrapper(draftOrdersWrapper);
        finremCaseDetails.setData(caseData);

        String refusalReason = "Insufficient information";
        LocalDateTime refusedDate = LocalDateTime.now();
        String judgeName = "Judge Doe";
        JudgeType judgeType = JudgeType.DISTRICT_JUDGE;
        String templateName = "templateName";
        String fileName = "fileName.dcc";
        String modifiedFilename = "fileName_ABC.doc";
        String caseId = "12345";
        CaseDocument expectedDocument = new CaseDocument();

        Map<String, Object> templateDetailsMap = Map.of("key", "value");

        // Mock dependencies
        when(contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(
            finremCaseDetails, finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList())).thenReturn(templateDetailsMap);
        when(documentConfiguration.getContestedDraftOrderNotApprovedTemplate(finremCaseDetails)).thenReturn(templateName);
        when(documentConfiguration.getContestedDraftOrderNotApprovedFileName()).thenReturn(fileName);
        try (MockedStatic<FileUtils> mockedStatic = mockStatic(FileUtils.class)) {
            mockedStatic.when(() -> FileUtils.insertTimestamp(fileName)).thenReturn(modifiedFilename);

            when(genericDocumentService.generateDocumentFromPlaceholdersMap(
                AUTH_TOKEN,
                templateDetailsMap,
                templateName,
                modifiedFilename, // Match the actual argument
                caseId
            )).thenReturn(expectedDocument);

            // Act
            CaseDocument result = underTest.generateRefuseOrder(
                finremCaseDetails, refusalReason, refusedDate, judgeName, judgeType, AUTH_TOKEN);

            // Assert
            assertNotNull(result);
            assertEquals(expectedDocument, result);

            // Verify that temporary values are cleared
            assertNull(draftOrdersWrapper.getGeneratedOrderReason());
            assertNull(draftOrdersWrapper.getGeneratedOrderRefusedDate());
            assertNull(draftOrdersWrapper.getGeneratedOrderJudgeName());
            assertNull(draftOrdersWrapper.getGeneratedOrderJudgeType());

            verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, templateDetailsMap, templateName, modifiedFilename,
                caseId);
            verifyNoMoreInteractions(genericDocumentService);
        }
    }
}
