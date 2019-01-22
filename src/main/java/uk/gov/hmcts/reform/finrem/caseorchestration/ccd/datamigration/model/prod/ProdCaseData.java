package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ProdCaseData {
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
    @JsonProperty("divorceUploadEvidence2")
    private CaseDocument divorceUploadEvidence2;

    // APPLICANT’S DETAILS
    @JsonProperty("applicantLName")
    private String applicantLName;
    @JsonProperty("applicantFMName")
    private String applicantFMName;

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

    // Mini Form A
    @JsonProperty("miniFormA")
    private CaseDocument miniFormA;

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
    @JsonProperty("amountToPay")
    private String amountToPay;
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
    @JsonProperty("assignedToJudgeReason")
    private String assignedToJudgeReason;
    @JsonProperty("assignedToJudge")
    private String assignedToJudge;

    @JsonProperty("referToJudgeText")
    private String referToJudgeText;
    @JsonProperty("referToJudgeTextFromAwaitingResponse")
    private String referToJudgeTextFromAwaitingResponse;
    @JsonProperty("referToJudgeTextFromOrderMade")
    private String referToJudgeTextFromOrderMade;
    @JsonProperty("referToJudgeTextFromConsOrdApproved")
    private String referToJudgeTextFromConsOrdApproved;
    @JsonProperty("referToJudgeTextFromConsOrdMade")
    private String referToJudgeTextFromConsOrdMade;
    @JsonProperty("referToJudgeTextFromClose")
    private String referToJudgeTextFromClose;
    @JsonProperty("referToJudgeTextFromRespondToOrder")
    private String referToJudgeTextFromRespondToOrder;
    @JsonProperty("uploadConsentOrderDocuments")
    private List<ConsentOrderData> uploadConsentOrderDocuments;
    @JsonProperty("uploadOrder")
    private List<ConsentOrderData> uploadOrder;
    @JsonProperty("uploadDocuments")
    private List<ConsentOrderData> uploadDocuments;
    @JsonProperty("generalOrderCollection")
    private List<GeneralOrderData> generalOrderCollection;
    @JsonProperty("solicitorAddress1")
    private String solicitorAddress1;
    @JsonProperty("solicitorAddress2")
    private String solicitorAddress2;
    @JsonProperty("solicitorAddress3")
    private String solicitorAddress3;
    @JsonProperty("solicitorAddress4")
    private String solicitorAddress4;
    @JsonProperty("solicitorAddress5")
    private String solicitorAddress5;
    @JsonProperty("solicitorAddress6")
    private String solicitorAddress6;
    @JsonProperty("rSolicitorAddress1")
    private String respondantSolicitorAddress1;
    @JsonProperty("rSolicitorAddress2")
    private String respondantSolicitorAddress2;
    @JsonProperty("responsdantSolicitorAddress3")
    private String respondantSolicitorAddress3;
    @JsonProperty("rSolicitorAddress4")
    private String responsdantSolicitorAddress4;
    @JsonProperty("rSolicitorAddress5")
    private String responsdantSolicitorAddress5;
    @JsonProperty("rSolicitorAddress6")
    private String responsdantSolicitorAddress6;
}
