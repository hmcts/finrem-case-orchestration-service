package uk.gov.hmcts.reform.finrem.finremcaseprogression.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseData {
    @JsonProperty("applicantLName")
    private String applicantLName;

    @JsonProperty("applicantFMName")
    private String applicantFMName;
}

/**
 "case_data": {
     "rSolicitorAddress1": "5 SOAP HOUSE LANE",
     "divorceUploadEvidence1": {
     "document_url": "http://document-management-store:8080/documents/90350339-3072-4b91-9e19-8140109c7e8b",
     "document_binary_url": "http://document-management-store:8080/documents/90350339-3072-4b91-9e19-8140109c7e8b/binary",
     "document_filename": "CCD configuration.txt"
     },
     "rSolicitorAddress4": "BRENTFORD",
     "rSolicitorAddress5": "TW8 0BT",
     "rSolicitorAddress2": null,
     "rSolicitorAddress3": null,
     "solicitorAgreeToReceiveEmails": "Yes",
     "natureOfApplication3": null,
     "solicitorEmail": "browns@mailinator.com",
     "natureOfApplication2": [
     "Secured Provision Order",
     "Pension Attachment Order"
     ],
     "rSolicitorPhone": "07562718466",
     "rSolicitorName": "Harry Downs",
     "solicitorDXnumber": null,
     "solicitorPaymentAccount": "1234567",
     "natureOfApplication7": null,
     "divorceStageReached": "Decree Nisi",
     "natureOfApplication6": [],
     "rSolicitorReference": null,
     "natureOfApplication9": "No",
     "applicantFMName": "Anna",
     "solicitorAddress3": null,
     "solicitorAddress2": "HACKNEY ROAD",
     "solicitorAddress1": "408",
     "rSolicitorFirm": "Downs Ltd",
     "authorisation2": "James Brown",
     "solicitorName": "Harry Brown",
     "authorisation3": "2018-06-06",
     "appRespondentRep": "Yes",
     "solicitorReference": null,
     "rSolicitorDXnumber": null,
     "d81Joint": {
     "document_url": "http://document-management-store:8080/documents/dc85da1c-ad13-4fe7-97fe-8c051ad7f6df",
     "document_binary_url": "http://document-management-store:8080/documents/dc85da1c-ad13-4fe7-97fe-8c051ad7f6df/binary",
     "document_filename": "d81-eng.pdf"
     },
     "otherCollection": [],
     "rSolicitorEmail": "downs@mailinator.com",
     "applicantLName": "Brown",
     "solicitorAddress5": "E2 7AP",
     "solicitorAddress4": "LONDON",
     "solicitorFirm": "Browns Ltd",
     "appRespondentLName": "Brown",
     "divorceCaseNumber": "EZ12D80123",
     "solicitorPhone": null,
     "consentOrder": {
     "document_url": "http://document-management-store:8080/documents/2450123e-6370-4810-8304-eb10068605b7",
     "document_binary_url": "http://document-management-store:8080/documents/2450123e-6370-4810-8304-eb10068605b7/binary",
     "document_filename": "HMCTS.txt"
     },
     "appRespondentFMName": "James",
     "d81Question": "Yes"
 }
 */
