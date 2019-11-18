package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;

import java.util.List;

@Getter
public class ExceptionRecord {

    private final String caseTypeId;
    private final String id;
    private final String poBox;
    private final List<OcrDataField> ocrDataFields;

    public ExceptionRecord(
            @JsonProperty("case_type_id") String caseTypeId,
            @JsonProperty("id") String id,
            @JsonProperty("po_box") String poBox,
            @JsonProperty("ocr_data_fields") List<OcrDataField> ocrDataFields
    ) {
        this.caseTypeId = caseTypeId;
        this.id = id;
        this.poBox = poBox;
        this.ocrDataFields = ocrDataFields;
    }
}