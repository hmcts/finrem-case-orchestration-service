package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import static org.assertj.core.api.Assertions.assertThat;

class GenericInputFieldsTest {

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    @NullSource
    void shouldReturnTrue(YesOrNo readyToSubmitDocument) {
        GenericInputFields genericInputFields = GenericInputFields.builder()
            .readyToSubmitDocument(readyToSubmitDocument).build();

        assertThat(genericInputFields.isReadyToSubmit()).isEqualTo(YesOrNo.isYes(readyToSubmitDocument));
    }
}
