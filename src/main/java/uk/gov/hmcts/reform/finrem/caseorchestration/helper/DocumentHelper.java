package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftHearingOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypedCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPOND_TO_ORDER_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isRespondentRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentHelper {

    public static final String DOCUMENT_BINARY_URL = "document_binary_url";
    public static final String ADDRESSEE = "addressee";
    public static final String CTSC_CONTACT_DETAILS = "ctscContactDetails";
    public static final String CASE_NUMBER = "caseNumber";

    private final ObjectMapper objectMapper;

    public CaseDocument getLatestAmendedConsentOrder(Map<String, Object> caseData) {
        Optional<AmendedConsentOrderData> reduce = ofNullable(caseData.get(AMENDED_CONSENT_ORDER_COLLECTION))
                .map(this::convertToAmendedConsentOrderDataList)
                .orElse(emptyList())
                .stream()
                .reduce((first, second) -> second);
        return reduce
                .map(consentOrderData -> consentOrderData.getConsentOrder().getAmendedConsentOrder())
                .orElseGet(() -> convertToCaseDocument(caseData.get(LATEST_CONSENT_ORDER)));
    }

    public List<CaseDocument> getPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(PENSION_DOCS_COLLECTION))
                .map(this::convertToPensionCollectionDataList)
                .orElse(emptyList())
                .stream()
                .map(PensionCollectionData::getTypedCaseDocument)
                .map(TypedCaseDocument::getPensionDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<CaseDocument> getFormADocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(FORM_A_COLLECTION))
            .map(this::convertToPensionCollectionDataList)
            .orElse(emptyList())
            .stream()
            .map(PensionCollectionData::getTypedCaseDocument)
            .map(TypedCaseDocument::getPensionDocument)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<CaseDocument> getConsentedInContestedPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION))
            .map(this::convertToPensionCollectionDataList)
            .orElse(emptyList())
            .stream()
            .map(PensionCollectionData::getTypedCaseDocument)
            .map(TypedCaseDocument::getPensionDocument)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public CaseDocument getLatestContestedDraftOrderCollection(Map<String, Object> caseData) {
        List<DraftHearingOrderData> contestedDraftOrders = ofNullable(caseData.get(DRAFT_HEARING_ORDER_COLLECTION))
            .map(this::convertToDraftHearingOrderDataList)
            .orElse(emptyList())
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!contestedDraftOrders.isEmpty()) {
            return contestedDraftOrders.get(contestedDraftOrders.size() - 1).getDraftHearingOrder().getCaseDocument();
        } else {
            return null;
        }


    }

    public CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private List<AmendedConsentOrderData> convertToAmendedConsentOrderDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<AmendedConsentOrderData>>() {
        });
    }

    private List<PensionCollectionData> convertToPensionCollectionDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<PensionCollectionData>>() {
        });
    }

    private List<DraftHearingOrderData> convertToDraftHearingOrderDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<DraftHearingOrderData>>() {
        });
    }

    private List<RespondToOrderData> convertToRespondToOrderDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<RespondToOrderData>>() {
        });
    }

    public List<GeneralLetterData> convertToGeneralLetterData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralLetterData>>() {
        });
    }

    public List<AdditionalHearingDocumentData> convertToAdditionalHearingDocumentData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<AdditionalHearingDocumentData>>() {
        });
    }

    public List<Map<String, Object>> convertToGenericList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private List<CaseDocument> convertToCaseDocumentList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<CaseDocument>>() {
        });
    }

    public Optional<CaseDocument> getLatestRespondToOrderDocuments(Map<String, Object> caseData) {
        Optional<RespondToOrderData> respondToOrderData = ofNullable(caseData.get(RESPOND_TO_ORDER_DOCUMENTS))
                .map(this::convertToRespondToOrderDataList)
                .orElse(emptyList())
                .stream()
                .filter(CommonFunction::isAmendedConsentOrderType)
                .reduce((first, second) -> second);
        if (respondToOrderData.isPresent()) {
            return respondToOrderData
                    .map(respondToOrderData1 -> respondToOrderData.get().getRespondToOrder().getDocumentLink());
        }

        return Optional.empty();
    }

    public CaseDetails prepareLetterToApplicantTemplateData(CaseDetails caseDetails) {
        // need to create a deep copy of CaseDetails.data, the copy is modified and sent later to Docmosis
        CaseDetails caseDetailsCopy = deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        boolean isConsentedApplication = isConsentedApplication(caseDetails);
        String reference = "";
        String addresseeName;
        Map addressToSendTo;
        String applicantName = buildFullName(caseData, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);

        if (isApplicantRepresentedByASolicitor(caseData)) {
            log.info("Applicant is represented by a solicitor");
            reference = nullToEmpty((caseData.get(SOLICITOR_REFERENCE)));
            addresseeName = nullToEmpty((caseData.get(isConsentedApplication ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME)));
            addressToSendTo = (Map) caseData.get(isConsentedApplication ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS);
        } else {
            log.info("Applicant is not represented by a solicitor");
            addresseeName = applicantName;
            addressToSendTo = (Map) caseData.get(APPLICANT_ADDRESS);
        }
        return prepareLetterTemplateData(caseDetailsCopy, reference, addresseeName, addressToSendTo, isConsentedApplication);
    }

    public CaseDetails prepareLetterToRespondentTemplateData(CaseDetails caseDetails) {
        // need to create a deep copy of CaseDetails.data, the copy is modified and sent later to Docmosis
        CaseDetails caseDetailsCopy = deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();
        boolean isConsentedApplication = isConsentedApplication(caseDetails);
        String reference = "";
        String addresseeName;
        Map addressToSendTo;

        String respondentName =  buildFullName(caseData,
            isConsentedApplication ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
            isConsentedApplication ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME);

        if (isRespondentRepresentedByASolicitor(caseDetails.getData())) {
            log.info("Respondent is represented by a solicitor");
            reference = nullToEmpty((caseData.get(RESP_SOLICITOR_REFERENCE)));
            addresseeName = nullToEmpty((caseData.get(RESP_SOLICITOR_NAME)));
            addressToSendTo = (Map) caseData.get(RESP_SOLICITOR_ADDRESS);
        } else {
            log.info("Respondent is not represented by a solicitor");
            addresseeName = respondentName;
            addressToSendTo = (Map) caseData.get(RESPONDENT_ADDRESS);
        }

        return prepareLetterTemplateData(caseDetailsCopy, reference, addresseeName, addressToSendTo, isConsentedApplication);
    }

    private CaseDetails prepareLetterTemplateData(CaseDetails caseDetailsCopy, String reference,
                                                  String addresseeName, Map addressToSendTo,
                                                  boolean isConsentedApplication) {

        Map<String, Object> caseData = caseDetailsCopy.getData();

        String ccdNumber = nullToEmpty((caseDetailsCopy.getId()));
        String applicantName = getApplicantFullName(caseDetailsCopy);
        String respondentName = getRespondentFullName(caseDetailsCopy, isConsentedApplication);

        if (addressLineOneAndPostCodeAreBothNotEmpty(addressToSendTo)) {
            Addressee addressee = Addressee.builder()
                .name(addresseeName)
                .formattedAddress(formatAddressForLetterPrinting(addressToSendTo))
                .build();

            caseData.put(CASE_NUMBER, ccdNumber);
            caseData.put("reference", reference);
            caseData.put(ADDRESSEE,  addressee);
            caseData.put("letterDate", String.valueOf(LocalDate.now()));
            caseData.put("applicantName", applicantName);
            caseData.put("respondentName", respondentName);
            caseData.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
            caseData.put("courtDetails", buildFrcCourtDetails(caseData, objectMapper));
        } else {
            log.info("Failed to prepare template data as not all required address details were present");
            throw new IllegalArgumentException("Mandatory data missing from address when trying to generate document");
        }

        return caseDetailsCopy;
    }

    public static CtscContactDetails buildCtscContactDetails() {
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

    public <T> T deepCopy(T object, Class<T> objectClass) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(object), objectClass);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    public String formatAddressForLetterPrinting(Map<String, Object> address) {
        return Stream.of("AddressLine1", "AddressLine2", "County", "PostTown", "PostCode")
            .map(address::get)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(StringUtils::isNotEmpty)
            .filter(s -> !s.equals("null"))
            .collect(Collectors.joining("\n"));
    }

    public BulkPrintDocument getCaseDocumentAsBulkPrintDocument(CaseDocument caseDocument) {
        return BulkPrintDocument.builder().binaryFileUrl(caseDocument.getDocumentBinaryUrl()).build();
    }

    public Optional<BulkPrintDocument> getDocumentLinkAsBulkPrintDocument(Map<String, Object> data, String documentName) {
        Map<String, Object> documentLink = (Map) data.get(documentName);

        return documentLink != null
            ? Optional.of(BulkPrintDocument.builder().binaryFileUrl(documentLink.get(DOCUMENT_BINARY_URL).toString()).build())
            : Optional.empty();
    }

    public List<BulkPrintDocument> getCollectionOfDocumentLinksAsBulkPrintDocuments(Map<String, Object> data, String collectionName,
                                                                                     String documentName) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        List<Map> documentList = ofNullable(data.get(collectionName))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        for (Map document : documentList) {
            Map value = (Map) document.get(VALUE);
            Map<String, Object> documentLink = (Map) value.get(documentName);
            if (documentLink != null) {
                bulkPrintDocuments.add(BulkPrintDocument.builder()
                    .binaryFileUrl(documentLink.get(DOCUMENT_BINARY_URL).toString())
                    .build());
            }
        }
        return bulkPrintDocuments;
    }

    public static String getApplicantFullName(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(),APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
    }

    private static String getRespondentFullName(CaseDetails caseDetails, boolean isConsentedApplication) {
        return isConsentedApplication
            ? getRespondentFullNameConsented(caseDetails) : getRespondentFullNameContested(caseDetails);
    }

    public static String getRespondentFullNameConsented(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(),CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, CONSENTED_RESPONDENT_LAST_NAME);
    }

    public static String getRespondentFullNameContested(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(),CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, CONTESTED_RESPONDENT_LAST_NAME);
    }

    public List<ContestedConsentOrderData> convertToContestedConsentOrderData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {});
    }

    public List<HearingOrderCollectionData> getFinalOrderDocuments(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData.get(FINAL_ORDER_COLLECTION),
            new TypeReference<>() {});
    }
}
