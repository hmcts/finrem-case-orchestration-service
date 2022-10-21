package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.GeneralApplicationCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.YesOrNo;

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
    private EvidenceParty generalApplicationReceivedFrom;
    private String generalApplicationDirectionsHearingTime;
    private String generalApplicationDirectionsHearingTimeEstimate;
    private String generalApplicationDirectionsAdditionalInformation;
    private String generalApplicationDirectionsRecitals;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationDirectionsCourtOrderDate;
    private String generalApplicationDirectionsTextFromJudge;
    private Document generalApplicationDirectionsDocument;
    private String generalApplicationNotApprovedReason;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationDirectionsHearingDate;
    private JudgeType generalApplicationDirectionsJudgeType;
    private String generalApplicationDirectionsJudgeName;
    private List<GeneralApplicationCollection> generalApplicationCollection;
    private String generalApplicationCreatedBy;
    private YesOrNo generalApplicationHearingRequired;
    private String generalApplicationTimeEstimate;
    private String generalApplicationSpecialMeasures;
    private Document generalApplicationDocument;
    private Document generalApplicationLatestDocument;
    private Document generalApplicationDraftOrder;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalApplicationLatestDocumentDate;
    private String generalApplicationPreState;
    private String generalApplicationReferToJudgeEmail;
    private String generalApplicationOutcomeOther;
    private GeneralApplicationOutcome generalApplicationOutcome;
}
