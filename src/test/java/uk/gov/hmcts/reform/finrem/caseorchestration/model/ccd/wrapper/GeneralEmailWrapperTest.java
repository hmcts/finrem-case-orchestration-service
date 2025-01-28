package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GeneralEmailWrapperTest {

    /**
     * Checks the {@code whenCreateGeneralEMailSubmitted} method that initialises {@link GeneralEmailWrapper}.
     * Confirms that the function clears working data, but leaves the email collection intact.
     * For GeneralEmailWrapper, working data is used to capture User input from an EXUI event.
     * For GeneralEmailWrapper, the GeneralEmailCollection is maintained as a record of emails sent.
     */
    @Test
    void givenContestedCase_whenCreateGeneralEMailSubmitted_thenGeneraLEmailWrapperCleared() {

        List<GeneralEmailCollection> emailCollection = List.of(
            GeneralEmailCollection.builder()
                .value(GeneralEmailHolder.builder()
                    .generalEmailBody("collection test body")
                    .generalEmailRecipient("collection recipient")
                    .generalEmailCreatedBy("collection sender")
                    .build())
                .build()
        );

        GeneralEmailWrapper generalEmailWrapper = GeneralEmailWrapper.builder()
            .generalEmailCollection(emailCollection)
            .generalEmailRecipient("working recipient")
            .generalEmailCreatedBy("working sender")
            .generalEmailUploadedDocument(new CaseDocument())
            .generalEmailBody("working test body")
            .build();

        generalEmailWrapper.setGeneralEmailValuesToNull();

        // Assert correct working fields are changed to null
        assertNull(generalEmailWrapper.getGeneralEmailRecipient());
        assertNull(generalEmailWrapper.getGeneralEmailCreatedBy());
        assertNull(generalEmailWrapper.getGeneralEmailUploadedDocument());
        assertNull(generalEmailWrapper.getGeneralEmailBody());

        // Validate the contents of GeneralEmailCollection are untouched
        assertThat(generalEmailWrapper.getGeneralEmailCollection()).hasSize(1);
        GeneralEmailHolder emailHolder = generalEmailWrapper.getGeneralEmailCollection().get(0).getValue();
        assertEquals("collection test body", emailHolder.getGeneralEmailBody());
        assertEquals("collection recipient", emailHolder.getGeneralEmailRecipient());
        assertEquals("collection sender", emailHolder.getGeneralEmailCreatedBy());
    }
}
