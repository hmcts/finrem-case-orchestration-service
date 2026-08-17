package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ct_hearingInformationCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDirectionDetail {
    @CCD(label = "Is this the final order?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isThisFinalYN;
    @CCD(label = "Is there another hearing to be listed ?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isAnotherHearingYN;
    @CCD(label = "Time Estimate", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private String timeEstimate;
    @CCD(label = "Hearing Date", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private LocalDate dateOfHearing;
    @CCD(label = "Hearing Time", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private String hearingTime;
    @CCD(label = " ", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
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
            label = "Type of hearing values",
            showCondition = "isAnotherHearingYN=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_hearingTypeDirections"
    )
    private HearingTypeDirection typeOfHearing;

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
