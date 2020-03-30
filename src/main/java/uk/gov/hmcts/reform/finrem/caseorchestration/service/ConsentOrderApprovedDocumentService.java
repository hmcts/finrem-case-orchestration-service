package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.LetterAddressHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ConsentOrderApprovedNotificationLetter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
public class ConsentOrderApprovedDocumentService extends AbstractDocumentService {

    private LetterAddressHelper letterAddressHelper;

    @Autowired
    public ConsentOrderApprovedDocumentService(DocumentClient documentClient,
                                               DocumentConfiguration config,
                                               ObjectMapper objectMapper,
                                               LetterAddressHelper letterAddressHelper) {
        super(documentClient, config, objectMapper);
        this.letterAddressHelper = letterAddressHelper;
    }

    public CaseDocument generateApprovedConsentOrderLetter(CaseDetails caseDetails, String authToken) {
        log.info(
                "Generating Approved Consent Order Letter {} from {} for bulk print for case id: {} ",
                config.getApprovedConsentOrderFileName(),
                config.getApprovedConsentOrderTemplate(),
                caseDetails.getId().toString());
        return generateDocument(authToken, caseDetails,
                config.getApprovedConsentOrderTemplate(),
                config.getApprovedConsentOrderFileName());
    }

    public CaseDocument generateApprovedConsentOrderNotificationLetter(CaseDetails caseDetails, String authToken) {

        log.info("Generating Approved Consent Order Notification Letter {} from {} for bulk print for case id: {} ",
                config.getApprovedConsentOrderFileName(),
                config.getApprovedConsentOrderTemplate(),
                caseDetails.getId().toString());

        Map<String, Object> caseData = caseDetails.getData();
        Map addressToSendTo;

        String ccdNumber = String.valueOf(caseDetails.getId());
        String reference = "";
        String addresseeName;
        String applicantName = join(nullToEmpty(caseData.get(APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_LAST_NAME_CCD_FIELD)));
        String respondentName = join(nullToEmpty(caseData.get(APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_RESP_LAST_NAME_CCD_FIELD)));

        String applicantRepresented = nullToEmpty(caseData.get(APPLICANT_REPRESENTED).toString());

        if (applicantRepresented.equals(YES_VALUE)) {
            log.info("Applicant is represented by a solicitor");
            reference = nullToEmpty(caseData.get(SOLICITOR_REFERENCE));
            addresseeName = nullToEmpty(caseData.get(SOLICITOR_NAME));
            addressToSendTo = (Map) caseData.get(APP_SOLICITOR_ADDRESS_CCD_FIELD);
        } else {
            log.info("Applicant is not represented by a solicitor");
            addresseeName = applicantName;
            addressToSendTo = (Map) caseData.get(APP_ADDRESS_CCD_FIELD);
        }

        if (addressLineOneAndPostCodeAreBothNotEmpty(addressToSendTo)) {
            Addressee addressee = Addressee.builder()
                    .name(addresseeName)
                    .formattedAddress(letterAddressHelper.formatAddressForLetterPrinting(addressToSendTo))
                    .build();

            ConsentOrderApprovedNotificationLetter consentOrderApprovedNotificationLetter =
                    ConsentOrderApprovedNotificationLetter.builder()
                            .caseNumber(ccdNumber)
                            .reference(reference)
                            .addressee(addressee)
                            .letterDate(String.valueOf(LocalDate.now()))
                            .applicantName(applicantName)
                            .respondentName(respondentName)
                            .build();

            caseData.put(CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER, consentOrderApprovedNotificationLetter);
        } else {
            log.info("Failed to generate Approved Consent Order Notification Letter as not all required address details were present");
            throw new IllegalArgumentException("Mandatory data missing from address when trying to generate letter");
        }

        return generateDocument(authToken, caseDetails,
                config.getApprovedConsentOrderNotificationTemplate(),
                config.getApprovedConsentOrderNotificationFileName());
    }

    public CaseDocument annexStampDocument(CaseDocument document, String authToken) {
        return super.annexStampDocument(document, authToken);
    }

    public List<PensionCollectionData> stampPensionDocuments(List<PensionCollectionData> pensionList,
                                                             String authToken) {
        return pensionList.stream()
                .map(data -> stampPensionDocuments(data, authToken)).collect(toList());
    }

    private PensionCollectionData stampPensionDocuments(PensionCollectionData pensionDocument, String authToken) {
        CaseDocument document = pensionDocument.getPensionDocumentData().getPensionDocument();
        CaseDocument stampedDocument = stampDocument(document, authToken);
        PensionCollectionData stampedPensionData = copyOf(pensionDocument);
        stampedPensionData.getPensionDocumentData().setPensionDocument(stampedDocument);
        return stampedPensionData;
    }

    private PensionCollectionData copyOf(PensionCollectionData pensionDocument) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(pensionDocument),
                    PensionCollectionData.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}