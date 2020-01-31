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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class OptionIdToValueTranslator {

    private FixedListOption fixedListOption;

    private final String optionsJsonFile;
    private final ObjectMapper objectMapper;

    @Autowired
    public OptionIdToValueTranslator(@Value("${optionsValueFile}") String optionsJsonFile,
                                     ObjectMapper objectMapper) {
        this.optionsJsonFile = optionsJsonFile;
        this.objectMapper = objectMapper;
    }

    Consumer<CaseDetails> translateOptionsValues = this::translateFixedListOptions;

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
        Optional.ofNullable(caseDetails.getData()).ifPresent(caseData -> {
            fixedListOption.optionsKeys().forEach(optionKey -> {
                handleTranslation(caseData, optionKey);
            });
        });
    }

    private void handleTranslation(Map<String, Object> data, String optionKey) {
        Object option = data.get(optionKey);

        if (option instanceof String) {
            String optionValue = optionsMap(optionKey).getOrDefault(option, (String) option);
            data.put(optionKey, optionValue);
        }

        if (option instanceof List) {
            List<String> originalOptionsList = (List<String>) option;
            Map<String, String> optionMap = optionsMap(optionKey);

            List<String> collect = originalOptionsList.stream()
                    .map(key -> optionMap.getOrDefault(key, key))
                    .collect(Collectors.toList());

            data.put(optionKey, collect);
        }
    }

    private Map<String, String> optionsMap(String optionKey) {
        return fixedListOption.optionMap(optionKey);
    }
}
