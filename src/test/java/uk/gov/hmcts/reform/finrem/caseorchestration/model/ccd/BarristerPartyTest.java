package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarristerPartyTest {

    @Test
    void testGetIntervenerBarristerByIndex() {
        assertThat(BarristerParty.getIntervenerBarristerByIndex(1)).isEqualTo(BarristerParty.INTERVENER1);
        assertThat(BarristerParty.getIntervenerBarristerByIndex(2)).isEqualTo(BarristerParty.INTERVENER2);
        assertThat(BarristerParty.getIntervenerBarristerByIndex(3)).isEqualTo(BarristerParty.INTERVENER3);
        assertThat(BarristerParty.getIntervenerBarristerByIndex(4)).isEqualTo(BarristerParty.INTERVENER4);
        assertThatThrownBy(() -> BarristerParty.getIntervenerBarristerByIndex(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
