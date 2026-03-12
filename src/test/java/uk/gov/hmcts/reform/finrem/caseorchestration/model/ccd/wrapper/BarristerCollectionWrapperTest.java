package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class BarristerCollectionWrapperTest {

    @Test
    void testGetIntervenerBarristersByIndex() {
        List<BarristerCollectionItem> intv1 = mock(List.class);
        List<BarristerCollectionItem> intv2 = mock(List.class);
        List<BarristerCollectionItem> intv3 = mock(List.class);
        List<BarristerCollectionItem> intv4 = mock(List.class);

        BarristerCollectionWrapper wrapper = BarristerCollectionWrapper.builder()
            .intvr1Barristers(intv1)
            .intvr2Barristers(intv2)
            .intvr3Barristers(intv3)
            .intvr4Barristers(intv4)
            .build();

        assertAll(
            () -> assertThat(wrapper.getIntervenerBarristersByIndex(1)).isEqualTo(intv1),
            () -> assertThat(wrapper.getIntervenerBarristersByIndex(2)).isEqualTo(intv2),
            () -> assertThat(wrapper.getIntervenerBarristersByIndex(3)).isEqualTo(intv3),
            () -> assertThat(wrapper.getIntervenerBarristersByIndex(4)).isEqualTo(intv4)
        );
    }

    @ParameterizedTest
    @EnumSource(IntervenerType.class)
    void shouldReturnCorrectBarristerListForIntervenerType(IntervenerType intervenerType) {
        List<BarristerCollectionItem> list1 = List.of(new BarristerCollectionItem());
        List<BarristerCollectionItem> list2 = List.of(new BarristerCollectionItem());
        List<BarristerCollectionItem> list3 = List.of(new BarristerCollectionItem());
        List<BarristerCollectionItem> list4 = List.of(new BarristerCollectionItem());

        BarristerCollectionWrapper wrapper = BarristerCollectionWrapper.builder()
            .intvr1Barristers(list1)
            .intvr2Barristers(list2)
            .intvr3Barristers(list3)
            .intvr4Barristers(list4)
            .build();

        List<BarristerCollectionItem> result = wrapper.getIntervenerBarristers(intervenerType);

        switch (intervenerType) {
            case INTERVENER_ONE -> assertThat(result).isEqualTo(list1);
            case INTERVENER_TWO -> assertThat(result).isEqualTo(list2);
            case INTERVENER_THREE -> assertThat(result).isEqualTo(list3);
            case INTERVENER_FOUR -> assertThat(result).isEqualTo(list4);
        }
    }
}
