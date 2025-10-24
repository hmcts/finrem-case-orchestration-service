package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicMultiSelectListTest {

    @Test
    void givenDynamicMultiSelectList_whenSetValueByCodes_thenReturnSameInstance() {
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder().code("1").build(),
                DynamicMultiSelectListElement.builder().code("2").build()
            ))
            .build();

        // Test that the method returns the instance itself.
        assertThat(dynamicMultiSelectList.setValueByCodes(List.of("1"))).isEqualTo(dynamicMultiSelectList);
    }

    @Test
    void givenDynamicMultiSelectList_whenSetValueByCodes_thenSetValueByMatchingCodes() {
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder().code("1").build(),
                DynamicMultiSelectListElement.builder().code("2").build()
            ))
            .build();
        assertThat(dynamicMultiSelectList.setValueByCodes(List.of("1", "2")).getValue())
            .extracting(DynamicMultiSelectListElement::getCode)
            .containsExactly("1", "2");
    }
}
