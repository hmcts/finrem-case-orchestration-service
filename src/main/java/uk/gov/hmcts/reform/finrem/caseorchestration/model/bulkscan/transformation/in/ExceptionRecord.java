package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.in.OcrDataField;

import java.util.List;

@Getter
@Builder
public class ExceptionRecord {

    @JsonProperty("case_type_id")
    private final String caseTypeId;

    @JsonProperty("id")
    private final String id;

    @JsonProperty("po_box")
    private final String poBox;

    @JsonProperty("form_type")
    private final String formType;

    @JsonProperty("ocr_data_fields")
    private final List<OcrDataField> ocrDataFields;

}