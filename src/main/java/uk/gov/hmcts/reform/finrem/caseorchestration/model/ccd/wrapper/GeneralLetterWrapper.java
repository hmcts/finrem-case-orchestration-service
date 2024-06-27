package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentsDiscovery;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;

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
public class GeneralLetterWrapper implements CaseDocumentsDiscovery {
    private DynamicRadioList generalLetterAddressee;
    private GeneralLetterAddressToType generalLetterAddressTo;
    private String generalLetterRecipient;
    private Address generalLetterRecipientAddress;
    private String generalLetterCreatedBy;
    private String generalLetterBody;
    private CaseDocument generalLetterPreview;
    private CaseDocument generalLetterUploadedDocument;
    private List<DocumentCollection> generalLetterUploadedDocuments;
    private List<GeneralLetterCollection> generalLetterCollection;

    @Override
    public List<CaseDocument> discover() {
        return Stream.of(
                Stream.of(generalLetterPreview, generalLetterUploadedDocument),
                ofNullable(generalLetterUploadedDocuments)
                    .orElse(List.of())
                    .stream()
                    .flatMap(d -> d.discover().stream()),
                ofNullable(generalLetterCollection)
                    .orElse(List.of())
                    .stream()
                    .flatMap(d -> d.discover().stream())
            )
            .flatMap(s -> s)
            .filter(Objects::nonNull)
            .toList();
    }
}
