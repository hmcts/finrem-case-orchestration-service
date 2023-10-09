package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantAndRespondentEvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralApplicationWrapper {
    private YesOrNo generalApplicationDirectionsHearingRequired;
    private String generalApplicationReceivedFrom;
    private ApplicantAndRespondentEvidenceParty appRespGeneralApplicationReceivedFrom;
    private String generalApplicationDirectionsHearingTime;
    private String generalApplicationDirectionsHearingTimeEstimate;
    private String generalApplicationDirectionsAdditionalInformation;
    private String generalApplicationDirectionsRecitals;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationDirectionsCourtOrderDate;
    private String generalApplicationDirectionsTextFromJudge;
    private CaseDocument generalApplicationDirectionsDocument;
    private List<GeneralApplicationsCollection> generalApplicationIntvrOrders;
    private String generalApplicationNotApprovedReason;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationDirectionsHearingDate;
    private JudgeType generalApplicationDirectionsJudgeType;
    private String generalApplicationDirectionsJudgeName;
    @JsonProperty("generalApplicationCollection")
    private List<GeneralApplicationCollection> generalApplicationDocumentCollection;
    private String generalApplicationCreatedBy;
    private YesOrNo generalApplicationHearingRequired;
    private String generalApplicationTimeEstimate;
    private String generalApplicationSpecialMeasures;
    private CaseDocument generalApplicationDocument;
    private CaseDocument generalApplicationLatestDocument;
    private CaseDocument generalApplicationDraftOrder;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationLatestDocumentDate;
    private String generalApplicationPreState;
    private String generalApplicationReferToJudgeEmail;
    private String generalApplicationOutcomeOther;
    private GeneralApplicationOutcome generalApplicationOutcome;
    private List<GeneralApplicationsCollection> generalApplications;
    private List<GeneralApplicationsCollection> appRespGeneralApplications;
    private List<GeneralApplicationsCollection> intervener1GeneralApplications;
    private List<GeneralApplicationsCollection> intervener2GeneralApplications;
    private List<GeneralApplicationsCollection> intervener3GeneralApplications;
    private List<GeneralApplicationsCollection> intervener4GeneralApplications;
    private String generalApplicationTracking;
    private String generalApplicationRejectReason;
    private DynamicList generalApplicationList;
    private DynamicList generalApplicationReferList;
    private String generalApplicationReferDetail;
    private DynamicList generalApplicationOutcomeList;
    private DynamicList generalApplicationDirectionsList;
}
