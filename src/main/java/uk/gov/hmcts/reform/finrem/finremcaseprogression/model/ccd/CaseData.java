package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
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
    private String rSolicitorName;
    @JsonProperty("rSolicitorFirm")
    private String rSolicitorFirm;
    @JsonProperty("rSolicitorReference")
    private String rSolicitorReference;
    @JsonProperty("rSolicitorAddress")
    private Address rSolicitorAddress;
    @JsonProperty("rSolicitorPhone")
    private String rSolicitorPhone;
    @JsonProperty("rSolicitorEmail")
    private String rSolicitorEmail;
    @JsonProperty("rSolicitorDXnumber")
    private String rSolicitorDXnumber;

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
    private List<DocumentData> pensionCollection;
    @JsonProperty("otherCollection")
    private List<DocumentData> otherCollection;

    // PAYMENT DETAILS
    @JsonProperty("helpWithFeesQuestion")
    private String helpWithFeesQuestion;
    @JsonProperty("HWFNumber")
    private String HWFNumber;
    @JsonProperty("feeAmountToPay")
    private String feeAmountToPay;
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
    private List<OrderRefusalData> orderRefusalCollection;
    @JsonProperty("dueDate")
    private LocalDate dueDate;
    @JsonProperty("issueDate")
    private LocalDate issueDate;
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
    @JsonProperty("referToJudgeTextFromConsentOrdApproved")
    private String referToJudgeTextFromConsentOrdApproved;
    @JsonProperty("referToJudgeTextFromConsOrdMade")
    private String referToJudgeTextFromConsOrdMade;
    @JsonProperty("uploadConsentOrderDocuments")
    private List<ConsentOrderData> uploadConsentOrderDocuments;
    @JsonProperty("uploadOrder")
    private List<ConsentOrderData> uploadOrder;
    @JsonProperty("uploadDocuments")
    private List<ConsentOrderData> uploadDocuments;
    @JsonProperty("generalOrderCollection")
    private List<GeneralOrderData> generalOrderCollection;
}

/**
 "case_data": {
     "PBANumber":"PBA123456",
     "d81Question":"No",
     "PBAreference":"ABCD",
     "consentOrder":{
         "document_url":"http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"
     },
     "d81Applicant":{
         "document_url":"http://document-management-store:8080/documents/2c9d3381-df6a-4817-aec3-a8a46ca0635b",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/2c9d3381-df6a-4817-aec3-a8a46ca0635b/binary"
     },
     "d81Respondent":{
         "document_url":"http://document-management-store:8080/documents/e284fb20-47c6-4b3a-bd17-cb005666ab5f",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/e284fb20-47c6-4b3a-bd17-cb005666ab5f/binary"
     },
     "solicitorFirm":"Mr",
     "solicitorName":"Solictor",
     "applicantLName":"Guy",
     "authorisation3":"2010-01-01",
     "feeAmountToPay":"150",
     "solicitorEmail":"test@admin.com",
     "solicitorPhone":"9963472494",
     "applicantFMName":"Poor",
     "authorisation2b":"test",
     "otherCollection":[
         {
             "id":"c0c5b8cc-8bb5-41da-84bf-06be40b8fa77",
             "value":{
             "typeOfDocument":"ScheduleOfAssets",
             "uploadedDocument":{
                 "document_url":"http://document-management-store:8080/documents/0abf044e-3d01-45eb-b792-c06d1e6344ee",
                 "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
                 "document_binary_url":"http://document-management-store:8080/documents/0abf044e-3d01-45eb-b792-c06d1e6344ee/binary"
                 }
             }
         }
     ],
     "respondentEmail":null,
     "respondentPhone":"9963472494",
     "appRespondentRep":"No",
     "consentOrderText":{
         "document_url":"http://document-management-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af69",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af69/binary"
     },
     "solicitorAddress":{
         "County":"test",
         "Country":"United Kingdom",
         "PostCode":"b1 1ab",
         "PostTown":"SRIKALAHASTI",
         "AddressLine1":"House no: 6-354-2, Gandhi Street",
         "AddressLine2":"Srikalahasti, Chittor District",
         "AddressLine3":"test"
     },
     "authorisationFirm":"test",
     "authorisationName":"test",
     "divorceCaseNumber":"DD12D12345",
     "pensionCollection":[
         {
         "id":"ad403dd7-75da-4ca6-8cf6-24a5e42f5bf4",
         "value":{
             "typeOfDocument":"Form P2",
             "uploadedDocument":{
                 "document_url":"http://document-management-store:8080/documents/fcecdc83-2070-4d3d-923e-5cca58f8a589",
                 "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
                 "document_binary_url":"http://document-management-store:8080/documents/fcecdc83-2070-4d3d-923e-5cca58f8a589/binary"
                 }
             }
         }
     ],
     "respondentAddress":{
         "County":"Essex",
         "Country":"United Kingdom",
         "PostCode":"SE12 9SE",
         "PostTown":"London",
         "AddressLine1":"252 Marvels Lane",
         "AddressLine2":"AddressGlobalUK",
         "AddressLine3":"London"
     },
     "solicitorDXnumber":null,
     "appRespondentLName":"Korivi",
     "solicitorReference":"LL01",
     "appRespondentFMName":"test",
     "divorceStageReached":"Decree Nisi",
     "helpWithFeesQuestion":"No",
     "natureOfApplication2":[
         "Lump Sum Order",
         "Periodical Payment Order",
         "Pension Sharing Order",
         "Pension Attachment Order",
         "Pension Compensation Sharing Order",
         "Pension Compensation Attachment Order",
         "A settlement or a transfer of property",
         "Property Adjustment  Order"
     ],
     "natureOfApplication5":"No",
     "natureOfApplication6":[
         "Step Child or Step Children",
         "disability expenses",
         "In addition to child support",
         "training",
         "When not habitually resident"
     ],
     "natureOfApplication7":"test",
     "natureOfApplication3a":"test",
     "natureOfApplication3b":"test",
     "divorceUploadEvidence1":{
         "document_url":"http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336",
         "document_filename":"WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg",
         "document_binary_url":"http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336/binary"
     },
     "orderForChildrenQuestion1":"Yes",
     "solicitorAgreeToReceiveEmails":"No"
     }
 */
