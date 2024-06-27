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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentsDiscovery;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailInterimCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

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
public class InterimWrapper implements CaseDocumentsDiscovery {
    private List<DirectionDetailInterimCollection> directionDetailsCollectionInterim;
    private String interimTimeEstimate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate interimHearingDate;
    private String interimHearingTime;
    private String interimAdditionalInformationAboutHearing;
    private YesOrNo interimPromptForAnyDocument;
    private InterimTypeOfHearing interimHearingType;
    private CaseDocument interimUploadAdditionalDocument;
    private CaseDocument interimHearingDirectionsDocument;
    private List<InterimHearingCollection> interimHearings;
    private List<InterimHearingCollection> interimHearingsScreenField;
    @JsonProperty("iHCollectionItemIds")
    private List<InterimHearingCollectionItemData> interimHearingCollectionItemIds;
    private List<InterimHearingBulkPrintDocumentsData> interimHearingDocuments;

    @Override
    public List<CaseDocument> discover() {
        return Stream.of(
                Stream.of(interimUploadAdditionalDocument, interimHearingDirectionsDocument),
                ofNullable(interimHearingDocuments).orElse(List.of()).stream()
                    .map(d -> ofNullable(d.getValue()).orElse(InterimHearingBulkPrintDocument.builder().build()).getCaseDocument())
            )
            .flatMap(s -> s)
            .filter(Objects::nonNull)
            .toList();
    }
}
