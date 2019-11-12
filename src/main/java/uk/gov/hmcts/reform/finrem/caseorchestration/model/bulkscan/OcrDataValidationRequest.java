package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class OcrDataValidationRequest {

    @ApiModelProperty(value = "List of ocr data fields to be validated.", required = true)
    @NotEmpty
    public final List<OcrDataField> ocrDataFields;

    public OcrDataValidationRequest(
            @JsonProperty("ocr_data_fields") List<OcrDataField> ocrDataFields
    ) {
        this.ocrDataFields = ocrDataFields;
    }

    public List<OcrDataField> getOcrDataFields() {
        return ocrDataFields;
    }
}

