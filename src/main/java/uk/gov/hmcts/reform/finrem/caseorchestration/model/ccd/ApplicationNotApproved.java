package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ct_applicationNotApproved", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationNotApproved {
    @CCD(label = "AND AFTER", searchable = false)
    private String andAfter;
    @CCD(label = "others (Please specify)", searchable = false)
    private String othersTextOrders;
    @CCD(
            label = "Reason For Refusal",
            hint = "Please tick all boxes that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_refusalReason"
    )
    private List<RefusalReason> reasonForRefusal;
    @CCD(
            label = "Select Judge",
            hint = "Please select the appropriate judge",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_Judge",
            typeParameterClass = FRFlJudge.class
    )
    private String selectJudge;
    @CCD(label = "Date of order", hint = "Date of order", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfOrder;
    @CCD(
            label = "Additional comments",
            hint = "Please add any additional comments for  court admin (this comments will not be accesible by applicant's solicitor)",
            searchable = false
    )
    private String additionalComments;
}
