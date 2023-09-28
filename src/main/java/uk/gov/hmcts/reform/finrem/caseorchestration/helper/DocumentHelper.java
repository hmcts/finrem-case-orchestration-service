package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_CARE_OF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PHONE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_PO_BOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_TOWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPOND_TO_ORDER_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentHelper {

    public static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";
    public static final String DOCUMENT_BINARY_URL = "document_binary_url";
    public static final String ADDRESSEE = "addressee";
    public static final String CTSC_CONTACT_DETAILS = "ctscContactDetails";
    public static final String CASE_NUMBER = "caseNumber";
    public static final String ORDER_TYPE = "orderType";
    public static final String VARIATION = "variation";
    public static final String CONSENT = "consent";


    public enum PaperNotificationRecipient {
        APPLICANT, RESPONDENT, SOLICITOR, APP_SOLICITOR, RESP_SOLICITOR,
        INTERVENER_ONE, INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR
    }


    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final GenericDocumentService service;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final LetterAddresseeGeneratorMapper letterAddresseeGenerator;

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

    public CaseDocument getLatestAmendedConsentOrder(FinremCaseData caseData) {
        Optional<AmendedConsentOrderCollection> reduce = ofNullable(caseData.getAmendedConsentOrderCollection())
            .orElse(emptyList())
            .stream()
            .reduce((first, second) -> second);
        return reduce
            .map(consentOrderData -> consentOrderData.getValue().getAmendedConsentOrder())
            .orElseGet(caseData::getLatestConsentOrder);
    }


    public List<CaseDocument> getPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(PENSION_DOCS_COLLECTION))
            .map(this::convertToPensionCollectionDataList)
            .orElse(emptyList())
            .stream()
            .map(PensionTypeCollection::getTypedCaseDocument)
            .map(PensionType::getPensionDocument)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Return List Object for given Case with the given indentation used.
     * <p>Please use @{@link #getFormADocumentsData(FinremCaseData)}</p>
     * @param caseData instance of Map
     * @return List Object
     * @deprecated Use {@link Map caseData}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    public List<CaseDocument> getFormADocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(FORM_A_COLLECTION))
            .map(this::convertToPaymentDocumentCollectionList)
            .orElse(emptyList())
            .stream()
            .map(PaymentDocumentCollection::getValue)
            .map(PaymentDocument::getUploadedDocument)
            .filter(Objects::nonNull)
            .toList();
    }

    public List<CaseDocument> getFormADocumentsData(FinremCaseData caseData) {
        return ofNullable(caseData.getCopyOfPaperFormA())
            .map(this::convertToPaymentDocumentCollectionList)
            .orElse(emptyList())
            .stream()
            .map(PaymentDocumentCollection::getValue)
            .map(PaymentDocument::getUploadedDocument)
            .filter(Objects::nonNull)
            .toList();
    }

    public List<CaseDocument> getConsentedInContestedPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION))
            .map(this::convertToPensionCollectionDataList)
            .orElse(emptyList())
            .stream()
            .map(PensionTypeCollection::getTypedCaseDocument)
            .map(PensionType::getPensionDocument)
            .filter(Objects::nonNull)
            .toList();
    }

    public boolean hasAnotherHearing(Map<String, Object> caseData) {
        List<DirectionDetailsCollectionData> directionDetailsCollectionList =
            convertToDirectionDetailsCollectionData(caseData
                .get(DIRECTION_DETAILS_COLLECTION_CT));

        // use a utility to handle directionDetailsCollectionList being null as well as empty
        return !CollectionUtils.isEmpty(directionDetailsCollectionList) && YES_VALUE.equalsIgnoreCase(
            nullToEmpty(directionDetailsCollectionList.get(0).getDirectionDetailsCollection().getIsAnotherHearingYN()));
    }

    public boolean hasAnotherHearing(FinremCaseData caseData) {
        List<DirectionDetailCollection> directionDetailsCollection
            = Optional.ofNullable(caseData.getDirectionDetailsCollection()).orElse(new ArrayList<>());
        Optional<DirectionDetailCollection> detailCollection
            = directionDetailsCollection.stream().filter(e -> e.getValue().getIsAnotherHearingYN().isYes()).findAny();
        return detailCollection.isPresent();
    }

    public CaseDocument getLatestGeneralOrder(Map<String, Object> caseData) {
        if (isNull(caseData.get(GENERAL_ORDER_LATEST_DOCUMENT))) {
            log.warn("Latest general order not found for printing for case");
            return null;
        }
        return convertToCaseDocument(caseData.get(GENERAL_ORDER_LATEST_DOCUMENT));
    }

    public CaseDocument getLatestGeneralOrder(FinremCaseData caseData) {
        if (isNull(caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument())) {
            log.warn("Latest general order not found for printing for case");
            return null;
        }
        return convertToCaseDocument(caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument());
    }

    public CaseDocument convertToCaseDocumentIfObjNotNull(Object object) {
        return object != null ? objectMapper.convertValue(object, CaseDocument.class) : null;
    }

    public CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    public CaseDocument convertToCaseDocument(Object object, Class<CaseDocument> toValueType) {
        return objectMapper.convertValue(object, toValueType);
    }

    private List<AmendedConsentOrderData> convertToAmendedConsentOrderDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private List<PensionTypeCollection> convertToPensionCollectionDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }


    private List<PaymentDocumentCollection> convertToPaymentDocumentCollectionList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }


    public List<String> convertToList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private List<RespondToOrderData> convertToRespondToOrderDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public List<GeneralLetterData> convertToGeneralLetterData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public CaseDocument getGeneralLetterUploadedDocument(Map<String, Object> caseData) {
        if (isNull(caseData.get(GENERAL_LETTER_UPLOADED_DOCUMENT))) {
            log.info("General letter uploaded document is not present for case");
            return null;
        }

        return convertToCaseDocument(caseData.get(GENERAL_LETTER_UPLOADED_DOCUMENT));
    }

    public List<DirectionDetailsCollectionData> convertToDirectionDetailsCollectionData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public List<AdditionalHearingDocumentData> convertToAdditionalHearingDocumentData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public List<PensionTypeCollection> getPensionDocuments(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData.get(PENSION_DOCS_COLLECTION), new TypeReference<>() {
        });
    }

    public Optional<CaseDocument> getLatestRespondToOrderDocuments(Map<String, Object> caseData) {
        Optional<RespondToOrderData> respondToOrderData = ofNullable(caseData.get(RESPOND_TO_ORDER_DOCUMENTS))
            .map(this::convertToRespondToOrderDataList)
            .orElse(emptyList())
            .stream()
            .filter(caseDataService::isAmendedConsentOrderType)
            .reduce((first, second) -> second);
        if (respondToOrderData.isPresent()) {
            return respondToOrderData
                .map(respondToOrderData1 -> respondToOrderData.get().getRespondToOrder().getDocumentLink());
        }

        return Optional.empty();
    }

    public Optional<CaseDocument> getLatestRespondToOrderDocuments(FinremCaseData caseData) {
        Optional<RespondToOrderDocumentCollection> respondToOrderDocumentCollection = ofNullable(caseData.getRespondToOrderDocuments())
            .orElse(emptyList())
            .stream()
            .filter(caseDataService::isAmendedConsentOrderTypeFR)
            .reduce((first, second) -> second);
        if (respondToOrderDocumentCollection.isPresent()) {
            return respondToOrderDocumentCollection.map(
                respondToOrderDataCollection1 -> respondToOrderDocumentCollection.get().getValue().getDocumentLink());
        }
        return Optional.empty();
    }

    public Optional<CaseDocument> getLatestAdditionalHearingDocument(Map<String, Object> caseData) {
        Optional<AdditionalHearingDocumentData> additionalHearingDocumentData =
            ofNullable(caseData.get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION))
                .map(this::convertToAdditionalHearingDocumentData)
                .orElse(emptyList())
                .stream()
                .reduce((first, second) -> second);
        if (additionalHearingDocumentData.isPresent()) {
            return additionalHearingDocumentData
                .map(additionalHearingDocumentDataCopy -> additionalHearingDocumentData.get().getAdditionalHearingDocument().getDocument());
        }
        return Optional.empty();
    }

    public Optional<CaseDocument> getLatestAdditionalHearingDocument(FinremCaseData caseData) {
        List<AdditionalHearingDocumentCollection> additionalHearingDocuments  =  caseData.getAdditionalHearingDocuments();
        Optional<AdditionalHearingDocumentCollection> documentCollection
            = ofNullable(additionalHearingDocuments)
            .orElse(emptyList())
            .stream()
            .reduce((first, second) -> second);
        if (documentCollection.isPresent()) {
            return documentCollection
                .map(documentCollectionCopy -> documentCollectionCopy.getValue().getDocument());
        }
        return Optional.empty();
    }

    /**
     * Return CaseDetails Object for given Case with the given indentation used.
     * <p>Please use @{@link #prepareLetterTemplateData(FinremCaseDetails, PaperNotificationRecipient)}</p>
     * @param caseDetails the casedetails
     * @param recipient instance of PaperNotificationRecipient
     * @return CaseDetails Object
     * @deprecated Use {@link FinremCaseDetails caseDetails, PaperNotificationRecipient recipient}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    public CaseDetails prepareLetterTemplateData(CaseDetails caseDetails, PaperNotificationRecipient recipient) {
        // need to create a deep copy of CaseDetails.data, the copy is modified and sent later to Docmosis
        CaseDetails caseDetailsCopy = deepCopy(caseDetails, CaseDetails.class);

        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        AddresseeDetails addresseeDetails = letterAddresseeGenerator.generate(caseDetailsCopy, recipient);
        return prepareLetterTemplateData(caseDetailsCopy, nullToEmpty(addresseeDetails.getReference()), addresseeDetails.getAddresseeName(),
            addresseeDetails.getAddressToSendTo(), isConsentedApplication);
    }

    public CaseDetails prepareLetterTemplateData(FinremCaseDetails caseDetails, PaperNotificationRecipient recipient) {

        AddresseeDetails addresseeDetails = letterAddresseeGenerator.generate(caseDetails, recipient);
        return prepareLetterTemplateData(caseDetails, nullToEmpty(addresseeDetails.getReference()), addresseeDetails.getAddresseeName(),
            addresseeDetails.getFinremAddressToSendTo());
    }


    /**
     * Return CaseDetails Object for given Case with the given indentation used.
     * <p>Please use @{@link #prepareLetterTemplateData(FinremCaseDetails, String, String, Address)}</p>
     *
     * @param caseDetailsCopy the casedetails
     * @param reference String
     * @param addresseeName String
     * @param addressToSendTo map
     * @param isConsentedApplication boolean
     * @return CaseDetails Object
     * @deprecated Use {@link CaseDetails caseDetails, String reference, String addresseeName,
     *                                                   Address addressToSendTo}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    private CaseDetails prepareLetterTemplateData(CaseDetails caseDetailsCopy, String reference, String addresseeName,
                                                  Map<String, Object> addressToSendTo,
                                                  boolean isConsentedApplication) {

        Map<String, Object> caseData = caseDetailsCopy.getData();

        String ccdNumber = nullToEmpty((caseDetailsCopy.getId()));
        String applicantName = getApplicantFullName(caseDetailsCopy);
        String respondentName = getRespondentFullName(caseDetailsCopy, isConsentedApplication);

        if (caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(addressToSendTo)) {
            Addressee addressee = Addressee.builder()
                .name(addresseeName)
                .formattedAddress(formatAddressForLetterPrinting(addressToSendTo))
                .build();

            caseData.put(CASE_NUMBER, ccdNumber);
            caseData.put("reference", reference);
            caseData.put(ADDRESSEE, addressee);
            caseData.put("letterDate", String.valueOf(LocalDate.now()));
            caseData.put("applicantName", applicantName);
            caseData.put("respondentName", respondentName);
            caseData.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
            caseData.put("courtDetails", buildFrcCourtDetails(caseData));
        } else {
            log.info("Failed to prepare template data as not all required address details were present");
            throw new IllegalArgumentException("Mandatory data missing from address when trying to generate document");
        }

        return caseDetailsCopy;
    }


    private CaseDetails prepareLetterTemplateData(FinremCaseDetails finremCaseDetails, String reference, String addresseeName,
                                                  Address addressToSendTo) {
        Long caseId = finremCaseDetails.getId();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        String ccdNumber = nullToEmpty((finremCaseDetails.getId()));
        String applicantName = finremCaseDetails.getData().getFullApplicantName();
        String respondentName = finremCaseDetails.getData().getRespondentFullName();

        if (addressLineOneAndPostCodeAreBothNotEmpty(addressToSendTo)) {
            Addressee addressee = Addressee.builder()
                .name(addresseeName)
                .formattedAddress(AddresseeGeneratorHelper.formatAddressForLetterPrinting(addressToSendTo))
                .build();

            caseData.put(CASE_NUMBER, ccdNumber);
            caseData.put("reference", reference);
            caseData.put(ADDRESSEE, addressee);
            caseData.put("letterDate", String.valueOf(LocalDate.now()));
            caseData.put("applicantName", applicantName);
            caseData.put("respondentName", respondentName);
            caseData.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
            caseData.put("courtDetails", buildFrcCourtDetails(finremCaseDetails.getData()));
        } else {
            log.info("Failed to prepare template data as not all required address details were present on case {}", caseId);
            throw new IllegalArgumentException("Mandatory data missing from address when trying to generate document");
        }

        return caseDetails;
    }

    public CaseDetails prepareIntervenerLetterTemplateData(FinremCaseDetails caseDetails, PaperNotificationRecipient recipient) {
        FinremCaseData caseData = caseDetails.getData();
        long caseId = caseDetails.getId();

        String reference = "";
        String addresseeName;
        Address addressToSendTo;

        boolean isIntervenerRepresented = checkIfIntervenerRepresentedBySolicitor(caseData.getCurrentIntervenerChangeDetails());

        if (isIntervenerPresent(recipient) && !isIntervenerRepresented) {
            log.info("Intervener One is not represented by a solicitor on case {}", caseId);
            addresseeName = caseData.getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerName();
            addressToSendTo = caseData.getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerAddress();
        } else {
            log.info("{} is not represented by a digital solicitor on case {}", recipient, caseId);
            ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
            addresseeName = recipient == APPLICANT
                ? caseDetails.getData().getFullApplicantName()
                : caseDetails.getData().getRespondentFullName();
            addressToSendTo = recipient == APPLICANT ? getApplicantCorrespondenceAddress(wrapper) :
                getRespondentCorrespondenceAddress(wrapper);
            if (recipient == APPLICANT && caseData.isApplicantRepresentedByASolicitor()) {
                reference = caseData.getContactDetailsWrapper().getSolicitorReference();
            }
            if (recipient == RESPONDENT && caseData.isRespondentRepresentedByASolicitor()) {
                reference = caseData.getContactDetailsWrapper().getRespondentSolicitorReference();
            }
        }

        return prepareLetterTemplateData(caseDetails, reference, addresseeName, addressToSendTo);
    }

    private boolean isIntervenerPresent(PaperNotificationRecipient recipient) {
        return recipient == INTERVENER_ONE || recipient == INTERVENER_TWO || recipient == INTERVENER_THREE || recipient == INTERVENER_FOUR;
    }

    private boolean addressLineOneAndPostCodeAreBothNotEmpty(Address address) {
        return ObjectUtils.isNotEmpty(address)
            && StringUtils.isNotEmpty(address.getAddressLine1())
            && StringUtils.isNotEmpty(address.getPostCode());
    }


    public <T> T deepCopy(T object, Class<T> objectClass) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(object), objectClass);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    public String formatAddressForLetterPrinting(Map<String, Object> address) {
        if (address != null) {
            return Stream.of("AddressLine1", "AddressLine2", "AddressLine3", "County", "PostTown", "PostCode")
                .map(address::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::isNotEmpty)
                .filter(s -> !s.equals("null"))
                .collect(Collectors.joining("\n"));
        }
        return "";
    }

    public BulkPrintDocument getCaseDocumentAsBulkPrintDocument(CaseDocument caseDocument) {
        return BulkPrintDocument.builder().binaryFileUrl(caseDocument.getDocumentBinaryUrl())
            .fileName(caseDocument.getDocumentFilename())
            .build();
    }

    public List<BulkPrintDocument> getCaseDocumentsAsBulkPrintDocuments(List<CaseDocument> caseDocuments) {
        return caseDocuments.stream()
            .map(caseDocument -> BulkPrintDocument.builder().binaryFileUrl(caseDocument.getDocumentBinaryUrl())
                .fileName(caseDocument.getDocumentFilename())
                .build())
            .toList();
    }

    public Optional<BulkPrintDocument> getDocumentLinkAsBulkPrintDocument(Map<String, Object> data, String documentName) {
        CaseDocument caseDocument = nullCheckAndConvertToCaseDocument(data.get(documentName));
        return caseDocument != null
            ? Optional.of(BulkPrintDocument.builder().binaryFileUrl(caseDocument.getDocumentBinaryUrl())
            .fileName(caseDocument.getDocumentFilename()).build())
            : Optional.empty();
    }

    public List<BulkPrintDocument> getHearingDocumentsAsBulkPrintDocuments(Map<String, Object> data,
                                                                           String authorisationToken,
                                                                           String caseId) {

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<DocumentCollection> pdfDocuments = new ArrayList<>();
        List<DocumentCollection> documentCollections = covertDocumentCollections(data.get(HEARING_ORDER_OTHER_COLLECTION));
        documentCollections.forEach(doc -> {
            CaseDocument caseDocument = doc.getValue();
            CaseDocument pdfDocument = service.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken, caseId);
            pdfDocuments.add(DocumentCollection
                .builder()
                .value(pdfDocument)
                .build());
            bulkPrintDocuments.add(getCaseDocumentAsBulkPrintDocument(pdfDocument));
        });

        data.put(HEARING_ORDER_OTHER_COLLECTION, pdfDocuments);
        return bulkPrintDocuments;
    }

    private List<DocumentCollection> covertDocumentCollections(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }
        return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public List<CaseDocument> getHearingDocumentsAsPdfDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData data = caseDetails.getData();
        List<CaseDocument> documents = new ArrayList<>();
        List<DocumentCollection> pdfDocuments = new ArrayList<>();
        List<DocumentCollection> documentCollections
            = Optional.ofNullable(data.getHearingOrderOtherDocuments()).orElse(new ArrayList<>());
        if (!documentCollections.isEmpty()) {
            documentCollections.forEach(doc -> {
                CaseDocument caseDocument = doc.getValue();
                CaseDocument pdfDocument = service.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken,
                    String.valueOf(caseDetails.getId()));
                pdfDocuments.add(DocumentCollection.builder().value(pdfDocument).build());
                documents.add(pdfDocument);
            });
            data.setHearingOrderOtherDocuments(pdfDocuments);
        }
        return documents;
    }

    public List<CaseDocument> getDocumentLinksFromCustomCollectionAsCaseDocuments(Map<String, Object> data, String collectionName,
                                                                                  String documentName) {
        List<CaseDocument> documents = new ArrayList<>();

        List<Map<String, Object>> documentList = ofNullable(data.get(collectionName))
            .map(i -> (List<Map<String, Object>>) i)
            .orElse(new ArrayList<>());

        for (Map<String, Object> document : documentList) {
            Map<String, Object> value = (Map<String, Object>) document.get(VALUE);
            getDocumentLinkAsCaseDocument(value, documentName).ifPresent(documents::add);
        }
        return documents;
    }

    public Optional<CaseDocument> getDocumentLinkAsCaseDocument(Map<String, Object> data, String documentName) {
        Map<String, Object> documentLink = documentName != null
            ? (Map<String, Object>) data.get(documentName)
            : data;

        return documentLink != null
            ? Optional.of(CaseDocument.builder()
            .documentUrl(documentLink.get(DOCUMENT_URL).toString())
            .documentFilename(documentLink.get(DOCUMENT_FILENAME).toString())
            .documentBinaryUrl(documentLink.get(DOCUMENT_BINARY_URL).toString())
            .build())
            : Optional.empty();
    }

    public String getApplicantFullName(CaseDetails caseDetails) {
        return caseDataService.buildFullName(caseDetails.getData(), APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
    }

    private String getRespondentFullName(CaseDetails caseDetails, boolean isConsentedApplication) {
        return isConsentedApplication
            ? getRespondentFullNameConsented(caseDetails) : getRespondentFullNameContested(caseDetails);
    }

    public String getRespondentFullNameConsented(CaseDetails caseDetails) {
        return caseDataService.buildFullName(caseDetails.getData(), CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, CONSENTED_RESPONDENT_LAST_NAME);
    }

    public String getRespondentFullNameContested(CaseDetails caseDetails) {
        return caseDataService.buildFullName(caseDetails.getData(), CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, CONTESTED_RESPONDENT_LAST_NAME);
    }

    private static Address getRespondentCorrespondenceAddress(ContactDetailsWrapper wrapper) {
        return wrapper.getContestedRespondentRepresented().isYes() ? wrapper.getRespondentSolicitorAddress() : wrapper.getRespondentAddress();
    }

    private static Address getApplicantCorrespondenceAddress(ContactDetailsWrapper wrapper) {
        return wrapper.getApplicantRepresented().isYes() ? wrapper.getApplicantSolicitorAddress() : wrapper.getApplicantAddress();
    }

    private boolean checkIfIntervenerRepresentedBySolicitor(IntervenerChangeDetails intervenerChangeDetails) {
        return YesOrNo.YES.equals(intervenerChangeDetails.getIntervenerDetails().getIntervenerRepresented());
    }

    public List<ContestedConsentOrderData> convertToContestedConsentOrderData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public List<HearingOrderCollectionData> getFinalOrderDocuments(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData.get(FINAL_ORDER_COLLECTION), new TypeReference<>() {
        });
    }

    public List<HearingOrderCollectionData> getHearingOrderDocuments(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData.get(HEARING_ORDER_COLLECTION),
            new TypeReference<>() {
            });
    }

    public BulkPrintDocument getBulkPrintDocumentFromCaseDocument(CaseDocument caseDocument) {
        return BulkPrintDocument.builder().binaryFileUrl(caseDocument.getDocumentBinaryUrl())
            .fileName(caseDocument.getDocumentFilename())
            .build();
    }

    public List<CaseDocument> getHearingNoticeDocuments(Map<String, Object> caseData) {
        return objectMapper.convertValue(caseData.get(HEARING_NOTICES_COLLECTION),
            new TypeReference<>() {
            });
    }

    public static PaperNotificationRecipient getIntervenerPaperNotificationRecipient(IntervenerWrapper intervenerWrapper) {
        if (IntervenerType.INTERVENER_ONE.equals(intervenerWrapper.getIntervenerType())) {
            return INTERVENER_ONE;
        } else if (IntervenerType.INTERVENER_TWO.equals(intervenerWrapper.getIntervenerType())) {
            return INTERVENER_TWO;
        } else if (IntervenerType.INTERVENER_THREE.equals(intervenerWrapper.getIntervenerType())) {
            return INTERVENER_THREE;
        } else if (IntervenerType.INTERVENER_FOUR.equals(intervenerWrapper.getIntervenerType())) {
            return INTERVENER_FOUR;
        }
        return null;
    }

    public CaseDocument nullCheckAndConvertToCaseDocument(Object object) {
        if (object != null) {
            return objectMapper.convertValue(object, CaseDocument.class);
        }
        return null;
    }

    public boolean isHighCourtSelected(Map<String, Object> caseData) {
        return caseData != null && caseData.get(HIGHCOURT_COURTLIST) != null;
    }

    public boolean isHighCourtSelected(FinremCaseData caseData) {
        Region region = caseData.getRegionWrapper().getDefaultRegionWrapper().getRegionList();
        return Region.HIGHCOURT.equals(region);
    }

    public StampType getStampType(Map<String, Object> caseData) {
        return isHighCourtSelected(caseData) ? StampType.HIGH_COURT_STAMP : StampType.FAMILY_COURT_STAMP;
    }

    public StampType getStampType(FinremCaseData caseData) {
        return isHighCourtSelected(caseData) ? StampType.HIGH_COURT_STAMP : StampType.FAMILY_COURT_STAMP;
    }
}
