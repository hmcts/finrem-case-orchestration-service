package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.removesolicitorfromcase;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Component
class RemoveSolicitorFromCaseFileReader {

    List<RemoveSolicitorFromCaseRequest> read(String filename) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvMapper
            .typedSchemaFor(RemoveSolicitorFromCaseRequest.class)
            .withHeader();

        URL src = getClass().getClassLoader().getResource(filename);
        if (src == null) {
            throw new IOException(String.format("File %s not found", filename));
        }

        MappingIterator<RemoveSolicitorFromCaseRequest> iterator = csvMapper
            .readerFor(RemoveSolicitorFromCaseRequest.class)
            .with(csvSchema)
            .readValues(src);

        return iterator.readAll();
    }
}
