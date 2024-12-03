package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RefusedOrder implements HasCaseDocument {

    private CaseDocument draftOrderOrPsa;

    private CaseDocument refusalOrder;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime refusedDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;

    private String submittedBy;

    private List<CaseDocumentCollection> attachments;

    private String refusalJudge;

}
