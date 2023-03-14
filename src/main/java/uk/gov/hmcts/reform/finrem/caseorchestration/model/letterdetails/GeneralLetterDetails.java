package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralLetterDetails implements DocumentTemplateDetails {
    @JsonProperty("generalLetterCreatedDate")
    private Date generalLetterCreatedDate;
    @JsonProperty("ccdCaseNumber")
    private String ccdCaseNumber;
    @JsonProperty("applicantFullName")
    private String applicantFullName;
    @JsonProperty("respondentFullName")
    private String respondentFullName;
    @JsonProperty("addressee")
    private Addressee addressee;
    @JsonProperty("reference")
    private String solicitorReference;
    private CtscContactDetails ctscContactDetails;
    private String generalLetterBody;

}
