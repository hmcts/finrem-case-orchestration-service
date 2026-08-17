package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ChildRelation;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_childrenCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChildDetails {

    @CCD(label = "Full Name", searchable = false)
    private String childFullname;
    @CCD(label = "Date of birth", searchable = false)
    private LocalDate childDateOfBirth;
    @CCD(
            label = "Gender",
            searchable = false,
            typeParameterOverride = "FR_childGender",
            typeParameterClass = FRChildGender.class
    )
    private Gender childGender;
    @CCD(
            label = "Relationship of applicant to the child",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_childRelation"
    )
    private ChildRelation childApplicantRelation;
    @CCD(
            label = "Please specify",
            showCondition = "childApplicantRelation=\"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String childApplicantRelationOther;
    @CCD(
            label = "Relationship of respondent to the child",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_childRelation"
    )
    private ChildRelation childRespondentRelation;
    @CCD(
            label = "Please specify",
            showCondition = "childRespondentRelation=\"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String childRespondentRelationOther;
    @CCD(label = "Does the child live in England or Wales?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("childrenLivesInEnglandOrWales")
    private YesOrNo childrenLiveInEnglandOrWales;
}
