package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import lombok.AllArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NullCheckerTest {

    @AllArgsConstructor
    static class NullCheckerTestFixture {
        String field1;
        Integer field2;
    }

    @ParameterizedTest
    @MethodSource
    void testAnyNonNull(Object object, boolean expected) {
        assertThat(NullChecker.anyNonNull(object)).isEqualTo(expected);
    }

    private static Stream<Arguments> testAnyNonNull() {
        return Stream.of(
            Arguments.of(new NullCheckerTestFixture(null, null), false),
            Arguments.of(new NullCheckerTestFixture("A", null), true),
            Arguments.of(new NullCheckerTestFixture(null, 1), true),
            Arguments.of(null, false),
            Arguments.of("anyString", true)
        );
    }
}
