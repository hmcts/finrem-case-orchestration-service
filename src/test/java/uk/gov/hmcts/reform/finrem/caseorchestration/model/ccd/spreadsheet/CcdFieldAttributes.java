package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import lombok.Data;

@Data
public class CcdFieldAttributes {
    private String fieldType;
    private String fieldId;
    private String fieldTypeParameter;
    private String listElementCode;
    private String listElementLabel;
}
