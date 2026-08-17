package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ct_draftDirectionDetailsCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftDirectionDetailsHolder {
    @CCD(
            label = "Is this the final order?",
            showCondition = "isThisFinalYN=\"DONOTSHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated
    private YesOrNo isThisFinalYN;
    @CCD(label = "Is there another hearing to be listed ?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isAnotherHearingYN;
    @CCD(
            label = "Type of hearing values",
            showCondition = "isAnotherHearingYN=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_hearingTypeDirections"
    )
    private HearingTypeDirection typeOfHearing;
    @CCD(label = "Time Estimate", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private HearingTimeDirection timeEstimate;
    @CCD(
            label = "Additional Time",
            showCondition = "isAnotherHearingYN=\"Yes\" AND timeEstimate=\"additionalTimeReq\"",
            searchable = false
    )
    private String additionalTime;
    @CCD(
            label = "This would usually be the applicants local Court",
            showCondition = "isAnotherHearingYN=\"Yes\"",
            searchable = false
    )
    private Court localCourt;
    @CCD(
            label = "Nottingham List",
            showCondition = "isAnotherHearingYN=\"Yes\" AND localCourt=\"nottingham\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class
    )
    private NottinghamCourt nottinghamList;
    @CCD(
            label = "CFC List",
            showCondition = "isAnotherHearingYN=\"Yes\" AND localCourt=\"cfc\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class
    )
    private CfcCourt cfcList;
    @CCD(
            label = "Any other listing instructions (Free Text)",
            showCondition = "isAnotherHearingYN=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String listingInstructor;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "#### Hearing Court",
          showCondition = "isAnotherHearingYN=\"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String hearingCourtLbl;
  // ==== end synthesised definition-only fields ====
}
