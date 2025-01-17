package uk.gov.hmcts.reform.finrem.caseorchestration.utils.generalemail;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
class GeneralEmailWrapperUtilsTest {

    /**
     * Parameterized test to check method that clears GeneralEmailWrapper.
     * Confirms that the function clears working data, but leaves the email collection intact.
     * For GeneralEmailWrapper, working data is used to capture User input from an EXUI event.
     * For GeneralEmailWrapper, the GeneralEmailCollection is maintained as a record of emails sent.
     * Tests that functionality consistent for contested and consented cases.
     *
     * @param caseDetails the {@link FinremCaseDetails} instance containing data to be cleared.
     */
    @ParameterizedTest
    @MethodSource("provideCaseDetails")
    void givenContestedCase_whenCreateGeneralEMailSubmitted_thenGeneraLEmailWrapperCleared(FinremCaseDetails caseDetails) {

        List<GeneralEmailCollection> emailCollection = List.of(
                GeneralEmailCollection.builder()
                        .value(GeneralEmailHolder.builder()
                                .generalEmailBody("collection test body")
                                .generalEmailRecipient("collection recipient")
                                .generalEmailCreatedBy("collection sender")
                                .build())
                        .build()
        );

        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailRecipient("working recipient");
        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailCreatedBy("working sender");
        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailUploadedDocument(new CaseDocument());
        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailBody("working test body");
        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailCollection(emailCollection);

        GeneralEmailWrapperUtils.setGeneralEmailValuesToNull(caseDetails);

        // Assert correct fields changed to null
        assertNull(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailRecipient());
        assertNull(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailCreatedBy());
        assertNull(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailUploadedDocument());
        assertNull(caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailBody());

        // Validate the contents of GeneralEmailCollection are untouched
        assertEquals(1, emailCollection.size());
        GeneralEmailHolder emailHolder = emailCollection.get(0).getValue();
        assertEquals("collection test body", emailHolder.getGeneralEmailBody());
        assertEquals("collection recipient", emailHolder.getGeneralEmailRecipient());
        assertEquals("collection sender", emailHolder.getGeneralEmailCreatedBy());
    }

    private static Stream<FinremCaseDetails> provideCaseDetails() {
        return Stream.of(
                FinremCaseDetailsBuilderFactory.from(123L, CaseType.CONTESTED).build(),
                FinremCaseDetailsBuilderFactory.from(456L, CaseType.CONSENTED).build()
        );
    }
}
