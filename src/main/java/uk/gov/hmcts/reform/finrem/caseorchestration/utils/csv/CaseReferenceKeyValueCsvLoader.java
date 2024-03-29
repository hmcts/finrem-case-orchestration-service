package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@NoArgsConstructor
@Component
public class CaseReferenceKeyValueCsvLoader {

    @SuppressWarnings({"java:S3740", "java:S1488"})
    public List<CaseReferenceKeyValue> loadCaseReferenceList(String fileName) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(CaseReferenceKeyValue.class).withHeader();
            List list = new CsvMapper().readerFor(CaseReferenceKeyValue.class)
                .with(csvSchema)
                .readValues(getClass().getClassLoader().getResource(fileName))
                .readAll();

            return list;
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

}
