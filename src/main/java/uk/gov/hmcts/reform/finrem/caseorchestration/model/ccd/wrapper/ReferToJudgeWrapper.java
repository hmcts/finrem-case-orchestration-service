package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

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

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReferToJudgeWrapper {
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDate;
    @CCD(ignore = true)
    private String referToJudgeText;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDateFromOrderMade;
    @CCD(ignore = true)
    private String referToJudgeTextFromOrderMade;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDateFromConsOrdApproved;
    @CCD(ignore = true)
    private String referToJudgeTextFromConsOrdApproved;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDateFromConsOrdMade;
    @CCD(ignore = true)
    private String referToJudgeTextFromConsOrdMade;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDateFromClose;
    @CCD(ignore = true)
    private String referToJudgeTextFromClose;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDateFromAwaitingResponse;
    @CCD(ignore = true)
    private String referToJudgeTextFromAwaitingResponse;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate referToJudgeDateFromRespondToOrder;
    @CCD(ignore = true)
    private String referToJudgeTextFromRespondToOrder;
}
