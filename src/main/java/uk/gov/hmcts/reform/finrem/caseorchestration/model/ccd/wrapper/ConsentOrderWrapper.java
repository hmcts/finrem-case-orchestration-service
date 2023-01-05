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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationDocumentTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentOrderWrapper {
    private List<DraftDirectionOrderCollection> draftDirectionOrderCollection;
    private DraftDirectionOrder latestDraftDirectionOrder;
    private List<DraftDirectionOrderCollection> judgesAmendedOrderCollection;
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollection;
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollectionRO;
    private List<NatureApplication> consentNatureOfApplicationChecklist;
    private String consentNatureOfApplicationAddress;
    private String consentNatureOfApplicationMortgage;
    private YesOrNo consentOrderForChildrenQuestion1;
    private YesOrNo consentNatureOfApplication5;
    private List<ConsentNatureOfApplication> consentNatureOfApplication6;
    private String consentNatureOfApplication7;
    private YesOrNo consentD81Question;
    private CaseDocument consentD81Joint;
    private CaseDocument consentD81Applicant;
    private CaseDocument consentD81Respondent;
    private List<OtherDocumentCollection> consentOtherCollection;
    @JsonProperty("consentOrderFRCName")
    private String consentOrderFrcName;
    @JsonProperty("consentOrderFRCAddress")
    private String consentOrderFrcAddress;
    @JsonProperty("consentOrderFRCEmail")
    private String consentOrderFrcEmail;
    @JsonProperty("consentOrderFRCPhone")
    private String consentOrderFrcPhone;
    private YesOrNo consentSubjectToDecreeAbsoluteValue;
    private YesOrNo consentServePensionProvider;
    private PensionProvider consentServePensionProviderResponsibility;
    private String consentServePensionProviderOther;
    private String consentSelectJudge;
    private String consentJudgeName;
    private List<ConsentOrderCollection> consentedNotApprovedOrders;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate consentDateOfOrder;
    private String consentAdditionalComments;
    private CaseDocument consentMiniFormA;
    private CaseDocument uploadConsentedOrder;
    @JsonProperty("Contested_ConsentedApprovedOrders")
    private List<ConsentOrderCollection> contestedConsentedApprovedOrders;
    private List<UploadConsentOrderCollection> uploadConsentOrder;
    private String consentVariationOrderLabelC;
    private String consentVariationOrderLabelL;
    private String otherDocLabel;
    private List<VariationDocumentTypeCollection> otherVariationCollection;
}
