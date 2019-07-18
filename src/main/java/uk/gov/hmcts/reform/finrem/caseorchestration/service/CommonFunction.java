package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class CommonFunction {

    public static BiFunction<Map<String, Object>, String, String> getString = (stringObjectMap, key) ->
        stringObjectMap.entrySet().stream()
            .filter(s -> s.getKey().equals(key))
            .map(s -> s.getValue())
            .map(Object::toString)
            .findFirst()
            .orElse(StringUtils.EMPTY);

    public static BiFunction<Map<String, Object>, String, Optional<Object>> getValue = (stringObjectMap, key) ->
        stringObjectMap.entrySet().stream()
            .filter(s -> s.getKey().equals(key))
            .map(s -> s.getValue())
            .findFirst();

}
