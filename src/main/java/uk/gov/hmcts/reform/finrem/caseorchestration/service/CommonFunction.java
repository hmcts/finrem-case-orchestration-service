package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonFunction {

    public static String nullToEmpty(Object o) {
        return o == null ? StringUtils.EMPTY : o.toString();
    }

    public static final Function<List<Map>, Map>
        getLastMapValue = (listMap) ->
        listMap.stream().reduce((first, second) -> second).get();

    public static final Function<List<Map>, Map>
        getFirstMapValue = (listMap) ->
        listMap.stream().findFirst().get();
}
