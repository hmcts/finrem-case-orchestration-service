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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ct_directionDetailCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DirectionDetail {
    @CCD(label = "Is there another hearing to be listed ?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isAnotherHearingYN;
    @CCD(label = "Time Estimate", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private String timeEstimate;
    @CCD(label = "Hearing Date", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfHearing;
    @CCD(label = "Hearing Time", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private String hearingTime;
    @CCD(label = " ", showCondition = "isAnotherHearingYN=\"Yes\"", searchable = false)
    private Court localCourt;
    /**
     * This field is no longer in use and is deprecated. Information should be stored in localCourt
     *
     * @deprecated This field is no longer in use and is deprecated. Information should be stored in localCourt
     */
    @CCD(
            label = "Nottingham List",
            showCondition = "isAnotherHearingYN=\"Yes\" AND localCourt=\"nottingham\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_NottinghamList",
            typeParameterClass = FRSNottinghamList.class
    )
    @Deprecated
    private NottinghamCourt nottinghamList;
    /**
     * This field is no longer in use and is deprecated. Information should be stored in localCourt
     *
     * @deprecated This field is no longer in use and is deprecated. Information should be stored in localCourt
     */
    @CCD(
            label = "CFC List",
            showCondition = "isAnotherHearingYN=\"Yes\" AND localCourt=\"cfc\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_CFCList",
            typeParameterClass = FRSCFCList.class
    )
    @Deprecated
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
