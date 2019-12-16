package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.ResolverStyle.STRICT;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BulkScanHelper {

    private static final DateTimeFormatter EXPECTED_DATE_FORMAT_FROM_FORM = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(STRICT);

    /**
     * Returns map with only the fields that were not blank from the OCR data.
     */
    public static Map<String, String> produceMapWithoutEmptyEntries(List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(field -> isNotBlank(field.getValue()))
            .collect(toMap(OcrDataField::getName, OcrDataField::getValue));
    }

    public static LocalDate transformFormDateIntoLocalDate(String formFieldName, String formDate) throws FormFieldValidationException {
        try {
            return LocalDate.parse(formDate, EXPECTED_DATE_FORMAT_FROM_FORM);
        } catch (DateTimeParseException exception) {
            throw new FormFieldValidationException(String.format("%s must be a valid date", formFieldName));
        }
    }

    public static Optional<List<String>> getCommaSeparatedValueFromOcrDataField(String fieldNameWithMultipleValues,
                                                                                List<OcrDataField> ocrDataFields) {
        //TODO clarify what the delimiter is - comma followed by space?
        //TODO modify tests if delimiter changes
        return ocrDataFields.stream()
            .filter(f -> f.getName().equals(fieldNameWithMultipleValues))
            .map(OcrDataField::getValue)
            .findFirst()
            .map(commaSeparatedString -> Splitter.on(", ").splitToList(commaSeparatedString));
    }

    public static Optional<String> commaSeparatedEntryTransformer(String fieldNameWithMultipleValues,
                                                                  List<OcrDataField> ocrDataFields,
                                                                  Map<String, String> ocrFieldToCcdFieldMap) {
        return getCommaSeparatedValueFromOcrDataField(fieldNameWithMultipleValues, ocrDataFields)
            .map(checkedElementsList ->
                checkedElementsList.stream()
                    .map(ocrFieldToCcdFieldMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()).toString()
            );
    }

    public static final Map<String, String> miamDomesticViolenceChecklistMap = new HashMap<String, String>() {{
        put("ArrestedRelevantDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_1");
        put("RelevantPoliceCautionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_2");
        put("RelevantCriminalProceedingsDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_3");
        put("blah", "ccd-blah-is-the-superior-blah");
        put("noWay", "placeholder1");
    }};

}