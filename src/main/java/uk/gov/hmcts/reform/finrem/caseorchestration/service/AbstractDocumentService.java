package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.LetterAddressHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Slf4j
public abstract class AbstractDocumentService {
    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    protected final DocumentConfiguration config;
    private final DocumentClient documentClient;
    protected final ObjectMapper objectMapper;

    private LetterAddressHelper letterAddressHelper = new LetterAddressHelper();

    public AbstractDocumentService(DocumentClient documentClient,
                                   DocumentConfiguration config,
                                   ObjectMapper objectMapper) {
        this.documentClient = documentClient;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    CaseDocument generateDocument(String authorisationToken, CaseDetails caseDetails,
                                          String template, String fileName) {

        Map<String, Object> caseDetailsMap = Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails);

        Document generatedPdf =
                documentClient.generatePdf(
                        DocumentGenerationRequest.builder()
                                .template(template)
                                .fileName(fileName)
                                .values(caseDetailsMap)
                                .build(),
                        authorisationToken);

        return caseDocument(generatedPdf);
    }

    UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
        return documentClient.bulkPrint(bulkPrintRequest);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentClient.deleteDocument(documentUrl, authorisationToken);
    }

    public CaseDocument annexStampDocument(CaseDocument document, String authorisationToken) {
        Document stampedDocument = documentClient.annexStampDocument(toDocument(document), authorisationToken);
        return caseDocument(stampedDocument);
    }

    public CaseDocument stampDocument(CaseDocument document, String authorisationToken) {
        Document stampedDocument = documentClient.stampDocument(toDocument(document), authorisationToken);
        return caseDocument(stampedDocument);
    }

    protected CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }

    protected Document toDocument(CaseDocument caseDocument) {
        Document document = new Document();
        document.setBinaryUrl(caseDocument.getDocumentBinaryUrl());
        document.setFileName(caseDocument.getDocumentFilename());
        document.setUrl(caseDocument.getDocumentUrl());
        return document;
    }

    CaseDetails copyOf(CaseDetails caseDetails) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    CaseDetails prepareNotificationLetter(CaseDetails caseDetails) {
        // need to create a deep copy of CaseDetails.data, the copy is modified and sent later to Docmosis
        CaseDetails caseDetailsCopy = caseDetails.toBuilder()
            .data(Maps.newHashMap(caseDetails.getData()))
            .build();
        Map<String, Object> docmosisCaseData = caseDetailsCopy.getData();
        Map addressToSendTo;

        String ccdNumber = nullToEmpty((caseDetailsCopy.getId()));
        String reference = "";
        String addresseeName;
        String applicantName = buildFullName(docmosisCaseData, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
        String respondentName =  buildFullName(docmosisCaseData, APP_RESPONDENT_FIRST_MIDDLE_NAME, APP_RESPONDENT_LAST_NAME);

        if (isApplicantRepresentedByASolicitor(docmosisCaseData)) {
            log.info("Applicant is represented by a solicitor");
            reference = nullToEmpty((docmosisCaseData.get(SOLICITOR_REFERENCE)));
            addresseeName = nullToEmpty((docmosisCaseData.get(SOLICITOR_NAME)));
            addressToSendTo = (Map) docmosisCaseData.get(APP_SOLICITOR_ADDRESS_CCD_FIELD);
        } else {
            log.info("Applicant is not represented by a solicitor");
            addresseeName = applicantName;
            addressToSendTo = (Map) docmosisCaseData.get(APPLICANT_ADDRESS);
        }

        if (addressLineOneAndPostCodeAreBothNotEmpty(addressToSendTo)) {
            Addressee addressee = Addressee.builder()
                .name(addresseeName)
                .formattedAddress(letterAddressHelper.formatAddressForLetterPrinting(addressToSendTo))
                .build();

            docmosisCaseData.put("caseNumber", ccdNumber);
            docmosisCaseData.put("reference", reference);
            docmosisCaseData.put("addressee",  addressee);
            docmosisCaseData.put("letterDate", String.valueOf(LocalDate.now()));
            docmosisCaseData.put("applicantName", applicantName);
            docmosisCaseData.put("respondentName", respondentName);
            docmosisCaseData.put("ctscContactDetails", buildCtscContactDetails());

        } else {
            log.info("Failed to generate notification letter as not all required address details were present");
            throw new IllegalArgumentException(
                "Mandatory data missing from address when trying to generate notification letter");
        }
        return caseDetailsCopy;
    }

    CtscContactDetails buildCtscContactDetails() {

        return CtscContactDetails.builder()
            .serviceCentre(CTSC_SERVICE_CENTRE)
            .careOf(CTSC_CARE_OF)
            .poBox(CTSC_PO_BOX)
            .town(CTSC_TOWN)
            .postcode(CTSC_POSTCODE)
            .emailAddress(CTSC_EMAIL_ADDRESS)
            .phoneNumber(CTSC_PHONE_NUMBER)
            .openingHours(CTSC_OPENING_HOURS)
            .build();
    }
}
