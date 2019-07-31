package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommonFunction {

    public static final BiFunction<Map<String, Object>, String, String> getString = (stringObjectMap, key) ->
        stringObjectMap.entrySet().stream()
            .filter(s -> s.getKey().equals(key))
            .map(s -> s.getValue())
            .map(Object::toString)
            .findFirst()
            .orElse(StringUtils.EMPTY);
    public static final BiFunction<Map<String, Object>, String, Optional<Object>> getValue = (stringObjectMap, key) ->
        stringObjectMap.entrySet().stream()
            .filter(s -> s.getKey().equals(key))
            .map(s -> s.getValue())
            .findFirst();
    public static final Function<List<Map>, Map>
        getLastMapValue = (listMap) ->
        listMap.stream().reduce((first, second) -> second).get();
    public static final Function<List<Map>, Map>
        getFirstMapValue = (listMap) ->
        listMap.stream().findFirst().get();

    private CommonFunction() {

    }


}
