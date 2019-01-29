package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class CaseData {
    // SOLICITOR DETAILS
    @JsonProperty("solicitorName")
    private String solicitorName;
    @JsonProperty("solicitorFirm")
    private String solicitorFirm;
    @JsonProperty("solicitorReference")
    private String solicitorReference;
    @JsonProperty("solicitorAddress")
    private Address solicitorAddress;
    @JsonProperty("solicitorPhone")
    private String solicitorPhone;
    @JsonProperty("solicitorEmail")
    private String solicitorEmail;
    @JsonProperty("solicitorDXnumber")
    private String solicitorDXnumber;
    @JsonProperty("solicitorAgreeToReceiveEmails")
    private String solicitorAgreeToReceiveEmails;

    // DIVORCE DETAILS
    @JsonProperty("divorceCaseNumber")
    private String divorceCaseNumber;
    @JsonProperty("divorceStageReached")
    private String divorceStageReached;
    @JsonProperty("divorceUploadEvidence1")
    private CaseDocument divorceUploadEvidence1;
    @JsonProperty("divorceDecreeNisiDate")
    private Date divorceDecreeNisiDate;
    @JsonProperty("divorceUploadEvidence2")
    private CaseDocument divorceUploadEvidence2;
    @JsonProperty("divorceDecreeAbsoluteDate")
    private Date divorceDecreeAbsoluteDate;

    // APPLICANT’S DETAILS
    @JsonProperty("applicantFMName")
    private String applicantFMName;
    @JsonProperty("applicantLName")
    private String applicantLName;

    // RESPONDENT DETAILS
    @JsonProperty("appRespondentFMName")
    private String appRespondentFMName;
    @JsonProperty("appRespondentLName")
    private String appRespondentLName;
    @JsonProperty("appRespondentRep")
    private String appRespondentRep;

    // RESPONDENT SOLICITOR’S DETAILS
    @JsonProperty("rSolicitorName")
    private String respondentSolicitorName;
    @JsonProperty("rSolicitorFirm")
    private String respondentSolicitorFirm;
    @JsonProperty("rSolicitorReference")
    private String respondentSolicitorReference;
    @JsonProperty("rSolicitorAddress")
    private Address respondentSolicitorAddress;
    @JsonProperty("rSolicitorPhone")
    private String respondentSolicitorPhone;
    @JsonProperty("rSolicitorEmail")
    private String respondentSolicitorEmail;
    @JsonProperty("rSolicitorDXnumber")
    private String respondentSolicitorDxNumber;

    // RESPONDENT SERVICE ADDRESS DETAILS
    @JsonProperty("respondentAddress")
    private Address respondentAddress;
    @JsonProperty("respondentPhone")
    private String respondentPhone;
    @JsonProperty("respondentEmail")
    private String respondentEmail;

    // NATURE OF THE APPLICATION
    @JsonProperty("natureOfApplication2")
    private List<String> natureOfApplication2;
    @JsonProperty("natureOfApplication3a")
    private String natureOfApplication3a;
    @JsonProperty("natureOfApplication3b")
    private String natureOfApplication3b;

    // ORDER FOR CHILDREN
    @JsonProperty("orderForChildrenQuestion1")
    private String orderForChildrenQuestion1;
    @JsonProperty("natureOfApplication5")
    private String natureOfApplication5;
    @JsonProperty("natureOfApplication6")
    private List<String> natureOfApplication6;
    @JsonProperty("natureOfApplication7")
    private String natureOfApplication7;

    // AUTHORISATION
    @JsonProperty("authorisationName")
    private String authorisationName;
    @JsonProperty("authorisationFirm")
    private String authorisationFirm;
    @JsonProperty("authorisation2b")
    private String authorisation2b;
    @JsonProperty("authorisation3")
    private Date authorisation3;

    // CONSENT ORDER
    @JsonProperty("consentOrder")
    private CaseDocument consentOrder;
    @JsonProperty("consentOrderText")
    private CaseDocument consentOrderText;

    // D81
    @JsonProperty("d81Question")
    private String d81Question;
    @JsonProperty("d81Joint")
    private CaseDocument d81Joint;
    @JsonProperty("d81Applicant")
    private CaseDocument d81Applicant;
    @JsonProperty("d81Respondent")
    private CaseDocument d81Respondent;

    // OPTIONAL DOCUMENTS
    @JsonProperty("pensionCollection")
    private List<DocumentData> pensionCollection;
    @JsonProperty("otherCollection")
    private List<DocumentData> otherCollection;

    // PAYMENT DETAILS
    @JsonProperty("helpWithFeesQuestion")
    private String helpWithFeesQuestion;
    @JsonProperty("HWFNumber")
    private String hwfNumber;
    @JsonProperty("PBANumber")
    private String pbaNumber;
    @JsonProperty("PBAreference")
    private String pbaReference;

    // ORDER DETAILS
    @JsonProperty("orderDirection")
    private String orderDirection;
    @JsonProperty("orderDirectionOpt1")
    private CaseDocument orderDirectionOpt1;
    @JsonProperty("orderDirectionOpt2")
    private String orderDirectionOpt2;
    @JsonProperty("orderDirectionAbsolute")
    private String orderDirectionAbsolute;
    @JsonProperty("orderDirectionJudge")
    private String orderDirectionJudge;
    @JsonProperty("orderDirectionJudgeName")
    private String orderDirectionJudgeName;
    @JsonProperty("orderDirectionDate")
    private Date orderDirectionDate;
    @JsonProperty("orderDirectionAddComments")
    private String orderDirectionAddComments;
    @JsonProperty("orderRefusalCollection")
    private List<OrderRefusalData> orderRefusalCollection;
    @JsonProperty("dueDate")
    private Date dueDate;
    @JsonProperty("issueDate")
    private Date issueDate;

    // ASSIGN TO JUDGE
    @JsonProperty("assignedToJudgeReason")
    private String assignedToJudgeReason;
    @JsonProperty("assignedToJudge")
    private String assignedToJudge;

    // REFER TO JUDGE DETAILS
    @JsonProperty("referToJudgeDate")
    private Date referToJudgeDate;
    @JsonProperty("referToJudgeText")
    private String referToJudgeText;
    @JsonProperty("referToJudgeDateFromOrderMade")
    private Date referToJudgeDateFromOrderMade;
    @JsonProperty("referToJudgeTextFromOrderMade")
    private String referToJudgeTextFromOrderMade;
    @JsonProperty("referToJudgeDateFromConsOrdApproved")
    private Date referToJudgeDateFromConsOrdApproved;
    @JsonProperty("referToJudgeTextFromConsOrdApproved")
    private String referToJudgeTextFromConsOrdApproved;
    @JsonProperty("referToJudgeDateFromConsOrdMade")
    private Date referToJudgeDateFromConsOrdMade;
    @JsonProperty("referToJudgeTextFromConsOrdMade")
    private String referToJudgeTextFromConsOrdMade;
    @JsonProperty("referToJudgeDateFromClose")
    private Date referToJudgeDateFromClose;
    @JsonProperty("referToJudgeTextFromClose")
    private String referToJudgeTextFromClose;
    @JsonProperty("referToJudgeDateFromAwaitingResponse")
    private Date referToJudgeDateFromAwaitingResponse;
    @JsonProperty("referToJudgeTextFromAwaitingResponse")
    private String referToJudgeTextFromAwaitingResponse;
    @JsonProperty("referToJudgeDateFromRespondToOrder")
    private Date referToJudgeDateFromRespondToOrder;
    @JsonProperty("referToJudgeTextFromRespondToOrder")
    private String referToJudgeTextFromRespondToOrder;

    // DIFFERENT DOCUMENT COLLECTIONS
    @JsonProperty("uploadConsentOrderDocuments")
    private List<ConsentOrderData> uploadConsentOrderDocuments;
    @JsonProperty("uploadOrder")
    private List<ConsentOrderData> uploadOrder;
    @JsonProperty("uploadDocuments")
    private List<ConsentOrderData> uploadDocuments;
    @JsonProperty("generalOrderCollection")
    private List<GeneralOrderData> generalOrderCollection;
    @JsonProperty("respondToOrderDocuments")
    private List<RespondToOrderData> respondToOrderDocuments;
    @JsonProperty("amendedConsentOrderCollection")
    private List<AmendedConsentOrderData> amendedConsentOrderCollection;
}
