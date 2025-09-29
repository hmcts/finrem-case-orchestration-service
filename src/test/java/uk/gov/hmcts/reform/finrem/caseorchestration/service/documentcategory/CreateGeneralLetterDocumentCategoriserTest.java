package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGeneralLetterDocumentCategoriserTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateGeneralLetterDocumentCategoriser underTest;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void givenFeatureToggleIsOn_whenGeneratedLetterDeleted_thenCategoriseWithoutException() {
        underTest.categorise(FinremCaseData.builder()
            .generalLetterWrapper(GeneralLetterWrapper.builder()
                .generalLetterCollection(List.of(
                    GeneralLetterCollection.builder()
                        .value(GeneralLetter.builder().generatedLetter(null).build())
                        .build()
                ))
                .build())
            .build());
    }

    @Test
    void givenFeatureToggleIsOn_whenGeneratedLetterExists_thenCategoryIdAssigned() {
        CaseDocument mockedCaseDocument = mock(CaseDocument.class);
        underTest.categorise(FinremCaseData.builder()
            .generalLetterWrapper(GeneralLetterWrapper.builder()
                .generalLetterCollection(List.of(
                    GeneralLetterCollection.builder()
                        .value(GeneralLetter.builder().generatedLetter(mockedCaseDocument).build())
                        .build()
                ))
                .build())
            .build());
        verify(mockedCaseDocument).setCategoryId(anyString());
    }
}
