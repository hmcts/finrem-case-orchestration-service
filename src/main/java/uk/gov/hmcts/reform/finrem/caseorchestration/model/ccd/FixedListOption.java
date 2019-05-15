package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FixedListOption {

    private Map<String, Map<String, String>> optionsMap = new HashMap<>();

    @JsonAnySetter
    public void addOption(String listKey, Map<String, String> options) {
        optionsMap.put(listKey, ImmutableMap.copyOf(options));
    }

    public Set<String> optionsKeys() {
        return ImmutableSet.copyOf(optionsMap.keySet());
    }

    public  Map<String, String> optionMap(String key) {
        return Optional.ofNullable(optionsMap.get(key)).orElse(ImmutableMap.of());
    }
}
