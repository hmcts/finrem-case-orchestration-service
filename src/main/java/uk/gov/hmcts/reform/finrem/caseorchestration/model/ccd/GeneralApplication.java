package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlEvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantAndRespondentEvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRSupportDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "generalApplicationCollection", generate = true)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralApplication implements HasCaseDocument {
    @CCD(
            label = "Upload General Application",
            hint = "Please upload a copy of the application as a Word, PDF, or Excel document (Word/Excel documents will be converted to PDF after submission).",
            regex = ".doc,.docx,.pdf,.xls,.xlsx",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("generalApplicationDocument")
    private CaseDocument generalApplicationDocument;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Application received from", searchable = false)
  private FRFlEvidenceParty generalApplicationReceivedFrom;
  @CCD(label = "Application received from", searchable = false, typeOverride = FieldType.DynamicRadioList)
  private String generalApplicationSender;
  @CCD(label = "Application received from", searchable = false)
  private ApplicantAndRespondentEvidenceParty appRespGeneralApplicationReceivedFrom;
  @CCD(label = "Application created by", searchable = false)
  private String generalApplicationCreatedBy;
  @CCD(label = "Is a hearing required?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo generalApplicationHearingRequired;
  @CCD(label = "Time estimate", searchable = false)
  private String generalApplicationTimeEstimate;
  @CCD(label = "Special measures", searchable = false)
  private String generalApplicationSpecialMeasures;
  @CCD(label = "Upload Supporting Document", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<FRSupportDocument>> gaSupportDocuments;
  @CCD(label = "Date Created", searchable = false)
  private java.time.LocalDate generalApplicationCreatedDate;
  @CCD(label = "Status", searchable = false)
  private String generalApplicationStatus;
  @CCD(label = "Outcome details", searchable = false)
  private String generalApplicationOutcomeOther;
  @CCD(
          label = "Upload Draft Order",
          hint = "Please upload a copy of the draft order as a Word, PDF, or Excel document (Word/Excel documents will be converted to PDF after submission)",
          regex = ".doc,.docx,.pdf,.xls,.xlsx",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.Document generalApplicationDraftOrder;
  @CCD(label = "Hearing / no hearing document", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.Document generalApplicationDirectionsDocument;
  @CCD(
          label = "Hearing details",
          showCondition = "hearingDetailsForGeneralApplication.tabHearingType=\"*\"",
          searchable = false
  )
  private HearingTabCollectionItem hearingDetailsForGeneralApplication;
  // ==== end synthesised definition-only fields ====
}
