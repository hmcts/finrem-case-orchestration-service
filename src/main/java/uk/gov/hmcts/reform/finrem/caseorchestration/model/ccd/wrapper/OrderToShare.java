package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Yes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderToShare {

    private String documentName;

    private YesOrNo documentToShare;

    private YesOrNo hasSupportingDocuments;
    
    private List<Yes> includeSupportingDocument;

    private List<AttachmentToShareCollection> attachmentsToShare;

    @JsonIgnore
    public YesOrNo getHasAttachment() {
        return YesOrNo.forValue(ofNullable(attachmentsToShare).isEmpty());
    }

}
