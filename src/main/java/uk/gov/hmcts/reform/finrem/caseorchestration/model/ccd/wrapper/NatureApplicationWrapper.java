package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildrenOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication5b;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRMsNatureApplication;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NatureApplicationWrapper {
    @CCD(
            label = "  ",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_ms_natureApplication",
            typeParameterClass = FRMsNatureApplication.class,
            access = {DefaultAccess.class}
    )
    private List<NatureApplication> natureOfApplicationChecklist;
    @CCD(ignore = true)
    private List<NatureApplication> natureOfApplication2;
    @CCD(ignore = true)
    private String natureOfApplication3a;
    @CCD(ignore = true)
    private String natureOfApplication3b;
    @CCD(ignore = true)
    private YesOrNo orderForChildrenQuestion1;
    @CCD(ignore = true)
    private YesOrNo natureOfApplication5;
    @CCD(ignore = true)
    private NatureApplication5b natureOfApplication5b;
    @CCD(ignore = true)
    private List<ChildrenOrder> natureOfApplication6;
    @CCD(ignore = true)
    private String natureOfApplication7;
}
