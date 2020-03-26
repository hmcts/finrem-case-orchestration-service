package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
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

    @Autowired
    public ConsentOrderApprovedDocumentService(DocumentClient documentClient,
                                               DocumentConfiguration config,
                                               ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
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

        Map<String, Object> caseData = caseDetails.getData();
        Map addressToSendTo;

        String ccdNumber = String.valueOf(caseDetails.getId());
        String recipientReference = "";
        String recipientName;
        String applicantName = join(nullToEmpty(caseData.get(APP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_LAST_NAME_CCD_FIELD)));
        String respondentName = join(nullToEmpty(caseData.get(APP_RESP_FIRST_AND_MIDDLE_NAME_CCD_FIELD)), " ",
                nullToEmpty(caseDetails.getData().get(APP_RESP_LAST_NAME_CCD_FIELD)));

        String applicantRepresented = nullToEmpty(caseData.get(APPLICANT_REPRESENTED).toString());

        if (applicantRepresented.equals(YES_VALUE)) {
            recipientReference = nullToEmpty(caseData.get(SOLICITOR_REFERENCE));
            recipientName = nullToEmpty(caseData.get(SOLICITOR_NAME));
            addressToSendTo = (Map) caseData.get(APP_SOLICITOR_ADDRESS_CCD_FIELD);
        } else {
            recipientName = applicantName;
            addressToSendTo = (Map) caseData.get(APP_ADDRESS_CCD_FIELD);
        }

        ConsentOrderApprovedNotificationLetter.ConsentOrderApprovedNotificationLetterBuilder consentOrderApprovedNotificationLetterBuilder =
                ConsentOrderApprovedNotificationLetter.builder()
                .ccdNumber(ccdNumber)
                .recipientRef(recipientReference)
                .recipientName(recipientName)
                .letterCreatedDate(String.valueOf(LocalDate.now()))
                .applicantName(applicantName)
                .respondentName(respondentName);

        if (addressLineOneAndPostCodeAreBothNotEmpty(addressToSendTo)) {
            caseData.put(CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER,
                    getConsentOrderApprovedNotificationLetter(consentOrderApprovedNotificationLetterBuilder, addressToSendTo));
        }

        log.info(
                "Generating Approved Consent Order Notification Letter {} from {} for bulk print for case id: {} ",
                config.getApprovedConsentOrderFileName(),
                config.getApprovedConsentOrderTemplate(),
                caseDetails.getId().toString());

        return generateDocument(authToken, caseDetails,
                config.getApprovedConsentOrderNotificationTemplate(),
                config.getApprovedConsentOrderNotificationFileName());
    }

    private ConsentOrderApprovedNotificationLetter getConsentOrderApprovedNotificationLetter(
            ConsentOrderApprovedNotificationLetter.ConsentOrderApprovedNotificationLetterBuilder consentOrderApprovedNotificationLetterBuilder,
            Map<String, Object> address) {

        return consentOrderApprovedNotificationLetterBuilder
                .addressLine1(nullToEmpty(address.get("AddressLine1")))
                .addressLine2(nullToEmpty(address.get("AddressLine2")))
                .addressLine3(nullToEmpty(address.get("AddressLine3")))
                .county(nullToEmpty(address.get("County")))
                .country(nullToEmpty(address.get("Country")))
                .postTown(nullToEmpty(address.get("PostTown")))
                .postCode(nullToEmpty(address.get("PostCode")))
                .build();
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