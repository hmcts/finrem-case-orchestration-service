package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.DirectionDetailInterimCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterimWrapper {
    private List<DirectionDetailInterimCollection> directionDetailsCollectionInterim;
    private String interimTimeEstimate;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate interimHearingDate;
    private String interimHearingTime;
    private String interimAdditionalInformationAboutHearing;
    private YesOrNo interimPromptForAnyDocument;
    private InterimTypeOfHearing interimHearingType;
    private Document interimUploadAdditionalDocument;
    private Document interimHearingDirectionsDocument;
    private List<InterimHearingCollection> interimHearings;
    @JsonProperty("iHCollectionItemIds")
    private List<InterimHearingCollectionItemData> interimHearingCollectionItemIds;
    private List<InterimHearingBulkPrintDocumentsData> interimHearingDocuments;
}
