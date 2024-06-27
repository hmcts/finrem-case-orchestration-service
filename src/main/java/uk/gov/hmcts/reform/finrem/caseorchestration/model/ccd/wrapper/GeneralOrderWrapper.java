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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentsDiscovery;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderAddressTo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralOrderWrapper implements CaseDocumentsDiscovery {
    private GeneralOrderAddressTo generalOrderAddressTo;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalOrderDate;
    private String generalOrderCreatedBy;
    private String generalOrderBodyText;
    private JudgeType generalOrderJudgeType;
    private String generalOrderRecitals;
    private String generalOrderJudgeName;
    private CaseDocument generalOrderLatestDocument;
    private CaseDocument generalOrderPreviewDocument;
    private List<ContestedGeneralOrderCollection> generalOrders;
    private List<ContestedGeneralOrderCollection> generalOrdersConsent;
    private List<GeneralOrderCollectionItem> generalOrderCollection;

    @Override
    public List<CaseDocument> discover() {
        return Stream.of(
                Stream.of(generalOrderLatestDocument, generalOrderPreviewDocument),
                ofNullable(generalOrders).orElse(List.of()).stream()
                    .map(d -> ofNullable(d.getValue()).orElse(ContestedGeneralOrder.builder().build()).getAdditionalDocument()),
                ofNullable(generalOrdersConsent).orElse(List.of()).stream()
                    .map(d -> ofNullable(d.getValue()).orElse(ContestedGeneralOrder.builder().build()).getAdditionalDocument()),
                ofNullable(generalOrderCollection).orElse(List.of()).stream()
                    .map(d -> ofNullable(d.getGeneralOrder()).orElse(GeneralOrder.builder().build()).getGeneralOrderDocumentUpload())
            )
            .flatMap(s -> s)
            .filter(Objects::nonNull)
            .toList();
    }
}
