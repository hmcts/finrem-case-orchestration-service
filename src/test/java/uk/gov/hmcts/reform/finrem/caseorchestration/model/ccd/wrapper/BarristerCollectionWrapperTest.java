package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BarristerCollectionWrapperTest {

    @Test
    void testGetIntervenerBarristersByIndex() {
        List<BarristerCollectionItem> intv1 = mock(List.class);
        List<BarristerCollectionItem> intv2 = mock(List.class);
        List<BarristerCollectionItem> intv3 = mock(List.class);
        List<BarristerCollectionItem> intv4 = mock(List.class);

        BarristerCollectionWrapper barristerCollectionWrapper = BarristerCollectionWrapper.builder()
            .intvr1Barristers(intv1)
            .intvr2Barristers(intv2)
            .intvr3Barristers(intv3)
            .intvr4Barristers(intv4)
            .build();

        assertThat(barristerCollectionWrapper.getIntervenerBarristersByIndex(1)).isEqualTo(intv1);
        assertThat(barristerCollectionWrapper.getIntervenerBarristersByIndex(2)).isEqualTo(intv2);
        assertThat(barristerCollectionWrapper.getIntervenerBarristersByIndex(3)).isEqualTo(intv3);
        assertThat(barristerCollectionWrapper.getIntervenerBarristersByIndex(4)).isEqualTo(intv4);
    }
}
