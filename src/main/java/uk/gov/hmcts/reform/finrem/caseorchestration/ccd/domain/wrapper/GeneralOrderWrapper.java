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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.GeneralOrderAddressTo;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.GeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.JudgeType;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralOrderWrapper {
    private GeneralOrderAddressTo generalOrderAddressTo;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalOrderDate;
    private String generalOrderCreatedBy;
    private String generalOrderBodyText;
    private JudgeType generalOrderJudgeType;
    private String generalOrderRecitals;
    private String generalOrderJudgeName;
    private Document generalOrderLatestDocument;
    private Document generalOrderPreviewDocument;
    private List<ContestedGeneralOrderCollection> generalOrders;
    private List<ContestedGeneralOrderCollection> generalOrdersConsent;
    private List<GeneralOrderCollection> generalOrderCollection;
}
