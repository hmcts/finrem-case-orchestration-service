package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ct_generalOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContestedGeneralOrder implements HasCaseDocument {
    @CCD(label = "Please type the order in the box ", searchable = false)
    private String generalOrderText;
    @CCD(label = "Document", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument additionalDocument;
    @CCD(
            label = "Select Judge",
            hint = "Please select the appropriate judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_Judge",
            typeParameterClass = FRFlJudge.class
    )
    @JsonProperty("selectJudge")
    private String judge;
    @CCD(label = "Court order date", hint = "Court order date", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfOrder;
    @CCD(
            label = "Additional comments",
            hint = "Please add any additional comments for  court admin (this comments will not be accessible by applicant's solicitor)",
            searchable = false
    )
    private String additionalComments;
    @CCD(
            label = "Addressed to",
            hint = "Please add any additional comments for  court admin (this comments will not be accessible by applicant's solicitor)",
            searchable = false
    )
    @JsonProperty("generalOrder_addressTo")
    private String generalOrderAddressTo;
}
