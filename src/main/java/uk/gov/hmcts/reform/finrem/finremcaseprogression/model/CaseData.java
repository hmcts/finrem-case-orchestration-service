package uk.gov.hmcts.reform.finrem.finremcaseprogression.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseData {
    // SOLICITOR DETAILS
    @JsonProperty("solicitorName")
    private String solicitorName;
    @JsonProperty("solicitorFirm")
    private String solicitorFirm;
    @JsonProperty("solicitorReference")
    private String solicitorReference;
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
    private String rSolicitorName;
    @JsonProperty("rSolicitorFirm")
    private String rSolicitorFirm;
    @JsonProperty("rSolicitorReference")
    private String rSolicitorReference;
    @JsonProperty("rSolicitorAddress1")
    private String rSolicitorAddress1;
    @JsonProperty("rSolicitorAddress2")
    private String rSolicitorAddress2;
    @JsonProperty("rSolicitorAddress3")
    private String rSolicitorAddress3;
    @JsonProperty("rSolicitorAddress4")
    private String rSolicitorAddress4;
    @JsonProperty("rSolicitorAddress5")
    private String rSolicitorAddress5;
    @JsonProperty("rSolicitorPhone")
    private String rSolicitorPhone;
    @JsonProperty("rSolicitorEmail")
    private String rSolicitorEmail;
    @JsonProperty("rSolicitorDXnumber")
    private String rSolicitorDXnumber;

    // RESPONDENT SERVICE ADDRESS DETAILS
    @JsonProperty("respondentAddress1")
    private String respondentAddress1;
    @JsonProperty("respondentAddress2")
    private String respondentAddress2;
    @JsonProperty("respondentAddress3")
    private String respondentAddress3;
    @JsonProperty("respondentAddress4")
    private String respondentAddress4;
    @JsonProperty("respondentAddress5")
    private String respondentAddress5;
    @JsonProperty("respondentAddress6")
    private String respondentAddress6;
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
    private LocalDate authorisation3;

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
    private List<PensionType> pensionCollection;
    @JsonProperty("otherCollection")
    private List<PensionType> otherCollection;

    // PAYMENT DETAILS
    @JsonProperty("helpWithFeesQuestion")
    private String helpWithFeesQuestion;
    @JsonProperty("HWFNumber")
    private String HWFNumber;
    @JsonProperty("PBANumber")
    private String PBANumber;
    @JsonProperty("PBAreference")
    private String PBAreference;

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
    private LocalDate orderDirectionDate;
    @JsonProperty("orderDirectionAddComments")
    private String orderDirectionAddComments;
    @JsonProperty("orderRefusalCollection")
    private List<String> orderRefusalCollection;
    @JsonProperty("dueDate")
    private LocalDate dueDate;
    @JsonProperty("issueDate")
    private LocalDate issueDate;
    @JsonProperty("referToJudgeText")
    private String referToJudgeText;
    @JsonProperty("referToJudgeTextFromAwaitingResponse")
    private String referToJudgeTextFromAwaitingResponse;
    @JsonProperty("referToJudgeTextFromOrderMade")
    private String referToJudgeTextFromOrderMade;
    @JsonProperty("referToJudgeTextFromConsentOrdApproved")
    private String referToJudgeTextFromConsentOrdApproved;
    @JsonProperty("referToJudgeTextFromConsOrdMade")
    private String referToJudgeTextFromConsOrdMade;
    @JsonProperty("uploadConsentOrderDocuments")
    private List<ConsentOrder> uploadConsentOrderDocuments;
    @JsonProperty("uploadOrder")
    private List<ConsentOrder> uploadOrder;
    @JsonProperty("uploadDocuments")
    private List<ConsentOrder> uploadDocuments;
    @JsonProperty("generalOrderCollection")
    private List<ConsentOrder> generalOrderCollection;
}

/**
 "case_data": {
     "d81Joint":{
         "document_url":"http://document-management-store:8080/documents/810e3c4b-4a79-4faa-aafd-0661da9da0e4",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/810e3c4b-4a79-4faa-aafd-0661da9da0e4/binary"
     },
     "HWFNumber":"1212121",
     "d81Question":"Yes",
     "consentOrder":{
         "document_url":"http://document-management-store:8080/documents/6ad58b2c-ff30-4181-abd1-02e728fdaafc",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/6ad58b2c-ff30-4181-abd1-02e728fdaafc/binary"
     },
     "solicitorFirm":"Mr",
     "solicitorName":"Solictor",
     "applicantLName":"Guy",
     "authorisation3":"2000-01-01",
     "solicitorEmail":"test@admin.com",
     "solicitorPhone":null,
     "applicantFMName":"Poor",
     "authorisation2b":"Sol3",
     "otherCollection":[],
     "respondentEmail":null,
     "respondentPhone":null,
     "appRespondentRep":"No",
     "authorisationFirm":"test",
     "authorisationName":"test",
     "divorceCaseNumber":"DD12D12345",
     "pensionCollection":[],
     "solicitorAddress1":"Allen Road",
     "solicitorAddress2":null,
     "solicitorAddress3":null,
     "solicitorAddress4":"dfd",
     "solicitorAddress5":"b1 1ab",
     "solicitorDXnumber":null,
     "appRespondentLName":"Bharatamma",
     "respondentAddress1":"dfdf",
     "respondentAddress2":"dfdf",
     "respondentAddress3":null,
     "respondentAddress4":"test",
     "respondentAddress5":"e61hq",
     "respondentAddress6":null,
     "solicitorReference":null,
     "appRespondentFMName":"test",
     "divorceStageReached":"Decree Nisi",
     "helpWithFeesQuestion":"Yes",
     "natureOfApplication2":[
        "Periodical Payment Order"
     ],
     "natureOfApplication3a":null,
     "natureOfApplication3b":null,
     "divorceUploadEvidence1":{
         "document_url":"http://document-management-store:8080/documents/4ce5bc61-d4b2-475f-8070-f1bb0c89f6b3",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/4ce5bc61-d4b2-475f-8070-f1bb0c89f6b3/binary"
     },
     "orderForChildrenQuestion1":"No",
     "solicitorAgreeToReceiveEmails":"No"
     }
 */
