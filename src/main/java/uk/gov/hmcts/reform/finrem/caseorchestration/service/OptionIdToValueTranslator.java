package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FixedListOption;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OptionIdToValueTranslator {

    private FixedListOption fixedListOption;

    @Value("${optionsValueFile}")
    private String optionsJsonFile;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void initOptionValueMap() {
        try {
            File file = ResourceUtils.getFile(optionsJsonFile);
            fixedListOption = objectMapper.readValue(file, FixedListOption.class);
        } catch (Exception e) {
            log.error("error reading options JSON file", e);
            fixedListOption = new FixedListOption();
        }
    }

    CaseDetails translateFixedListOptions(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        fixedListOption.optionsKeys().forEach(optionKey -> {
            Object option = data.get(optionKey);
            Map<String, String> optionMap = fixedListOption.optionMap(optionKey);

            if (option instanceof String) {
                String optionValue = optionMap.getOrDefault(option, (String) option);
                data.put(optionKey, optionValue);
            }

            if (option instanceof List) {
                List<String> originalOptionsList = (List<String>) option;
                List<String> collect = originalOptionsList.stream()
                        .map(key -> optionMap.getOrDefault(key, (String) key))
                        .collect(Collectors.toList());

                data.put(optionKey, collect);
            }
        });

        return caseDetails;
    }
}
