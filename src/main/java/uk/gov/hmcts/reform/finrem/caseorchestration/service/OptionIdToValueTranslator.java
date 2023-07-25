package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FixedListOption;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class OptionIdToValueTranslator {

    private final String optionsJsonFile;
    private final ObjectMapper objectMapper;
    private FixedListOption fixedListOption;
    Consumer<CaseDetails> translateOptionsValues = this::translateFixedListOptions;

    @Autowired
    public OptionIdToValueTranslator(@Value("${optionsValueFile}") String optionsJsonFile, ObjectMapper objectMapper) {
        this.optionsJsonFile = optionsJsonFile;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void initOptionValueMap() {
        try {
            fixedListOption = objectMapper.readValue(optionsJson(), FixedListOption.class);
        } catch (Exception error) {
            throw new IllegalStateException(String.format("error reading %s", optionsJsonFile), error);
        }
    }

    private String optionsJson() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(optionsJsonFile)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    public void translateFixedListOptions(CaseDetails caseDetails) {
        Optional.ofNullable(caseDetails.getData()).ifPresent(caseData ->
            fixedListOption.optionsKeys().forEach(optionKey ->
                handleTranslation(caseData, optionKey)));
    }

    private void handleTranslation(Map<String, Object> data, String optionKey) {
        Object option = data.get(optionKey);

        if (option instanceof String opt) {
            String optionValue = optionsMap(optionKey).getOrDefault(option, opt);
            data.put(optionKey, optionValue);
        }

        if (option instanceof List) {
            List<String> originalOptionsList = (List<String>) option;
            Map<String, String> optionMap = optionsMap(optionKey);

            List<String> collect = new ArrayList<>();
            originalOptionsList.forEach(key -> collect.add(optionMap.getOrDefault(key, key)));

            data.put(optionKey, collect);
        }
    }

    private Map<String, String> optionsMap(String optionKey) {
        return fixedListOption.optionMap(optionKey);
    }
}
