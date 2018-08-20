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
     "rSolicitorAddress1": {
         "County": "",
         "Country": "United Kingdom",
         "PostCode": "M5 4WT",
         "PostTown": "Salford",
         "AddressLine1": "43 Crescent",
         "AddressLine2": "",
         "AddressLine3": ""
     },
     "divorceUploadEvidence1": {
         "document_url": "http://dm-store-aat.service.core-compute-aat.internal:443/documents/5fd765ab-412d-4804-8d76-85f2f1596cbb",
         "document_filename": "d81-eng.pdf",
         "document_binary_url": "http://dm-store-aat.service.core-compute-aat.internal:443/documents/5fd765ab-412d-4804-8d76-85f2f1596cbb/binary"
     },
     "solicitorAgreeToReceiveEmails": "Yes",
     "solicitorEmail": "test@gmail.com",
     "rSolicitorPhone": null,
     "natureOfApplication2": [
         "Periodical Payment Order",
         "Pension Attachment Order"
     ],
     "rSolicitorName": "David Lawyer",
     "solicitorDXnumber": null,
     "divorceStageReached": "Decree Nisi",
     "rSolicitorReference": null,
     "authorisationName": "Mike Ross",
     "applicantFMName": "Minnie",
     "solicitorAddress1": {
         "County": "",
         "Country": "United Kingdom",
         "PostCode": "SE16 5UD",
         "PostTown": "London",
         "AddressLine1": "Flat 98",
         "AddressLine2": "Tivoli Court",
         "AddressLine3": "Rotherhithe Street"
     },
     "rSolicitorFirm": "Best Law",
     "solicitorName": "Mike Ross",
     "authorisation3": "2018-08-14",
     "appRespondentRep": "Yes",
     "solicitorReference": null,
     "rSolicitorDXnumber": null,
     "d81Joint": {
         "document_url": "http://dm-store-aat.service.core-compute-aat.internal:443/documents/5c320269-a2d5-41bd-a1c8-aea6c6368fcf",
         "document_filename": "d81-eng.pdf",
         "document_binary_url": "http://dm-store-aat.service.core-compute-aat.internal:443/documents/5c320269-a2d5-41bd-a1c8-aea6c6368fcf/binary"
     },
     "authorisation2b": "Partner",
     "pensionCollection": [
         {
             "id": "031c8c48-5324-4748-b3b0-35ad92adf08a",
             "value": {
                "typeOfDocument": "Form P2"
             }
         }
     ],
     "otherCollection": [
         {
             "id": "a03a2c2b-e68a-4aa3-9a3c-752c8374b8c1",
             "value": {
                "typeOfDocument": ""
             }
         }
     ],
     "applicantLName": "Mouse",
     "rSolicitorEmail": null,
     "solicitorFirm": "London Law",
     "appRespondentLName": "Mouse",
     "natureOfApplication3b": null,
     "natureOfApplication3a": null,
     "divorceCaseNumber": "LV12D81234",
     "solicitorPhone": "",
     "HWFNumber": "PBA1234567",
     "consentOrder": {
         "document_url": "http://dm-store-aat.service.core-compute-aat.internal:443/documents/ffc69fd3-5e80-45a6-9e2d-e5e7bacf3890",
         "document_filename": "d81-eng.pdf",
         "document_binary_url": "http://dm-store-aat.service.core-compute-aat.internal:443/documents/ffc69fd3-5e80-45a6-9e2d-e5e7bacf3890/binary"
     },
     "helpWithFeesQuestion": "Yes",
     "authorisationFirm": "London Law",
     "orderForChildrenQuestion1": "No",
     "d81Question": "Yes",
     "appRespondentFMName": "Mickey"
     }
 */
