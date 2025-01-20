package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderToShare {

    private DynamicMultiSelectList documentToShare;

    private YesOrNo hasSupportingDocuments;
    
    private YesOrNo includeSupportingDocument;

    private DynamicMultiSelectList attachmentsToShare;

    @JsonIgnore
    public YesOrNo getHasAttachment() {
        return YesOrNo.forValue(ofNullable(attachmentsToShare).map(DynamicMultiSelectList::getListItems).isEmpty());
    }

}
