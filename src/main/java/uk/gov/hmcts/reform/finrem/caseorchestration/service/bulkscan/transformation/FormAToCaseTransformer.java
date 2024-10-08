package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.InputScannedDoc;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ComplexTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedD81Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypedCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ChildrenInfoMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.mappers.ContactDetailsMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.bsp.common.mapper.GenericMapper.getValueFromOcrDataFields;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.getCommaSeparatedValuesFromOcrDataField;
import static uk.gov.hmcts.reform.bsp.common.utils.BulkScanCommonHelper.transformFormDateIntoCcdDate;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.COVER_LETTER_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.D81_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DECREE_NISI_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DRAFT_CONSENT_ORDER_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.FORM_A_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.FORM_E_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.OTHER_SUPPORT_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.P1_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.P2_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PPF1_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PPF2_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PPF_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.domain.FormA.ApplicantRepresentedPaper.FR_APPLICANT_REPRESENTED_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED_PAPER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_CONTROL_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_DELIVERY_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_EXCEPTIONAL_RECORD_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_SCANNED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_SUBTYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_CONTROL_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_DELIVERY_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_EXCEPTIONAL_RECORD_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_SCANNED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_SUBTYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORMA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.applicantRepresentedPaperToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.natureOfApplicationChecklistToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.orderForChildrenNoAgreementToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.orderForChildrenToCcdFieldNames;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.provisionMadeForToCcdFieldNames;

@Slf4j
@RequiredArgsConstructor
@Component
public class FormAToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;
    private static final String NATURE_OF_APP_5B = "natureOfApplication5b";

    static {
        ocrToCCDMapping = formAExceptionRecordToCcdMap();
    }

    private final CaseDataService caseDataService = new CaseDataService(new ObjectMapper());

    private static Map<String, String> formAExceptionRecordToCcdMap() {
        Map<String, String> exceptionRecordToCcdFieldsMap = new HashMap<>();

        // Section 0 - nature of application
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.DIVORCE_CASE_NUMBER, CCDConfigConstant.DIVORCE_CASE_NUMBER);
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.HWF_NUMBER, "HWFNumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_INTENDS_TO, "applicantIntendsTo");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLYING_FOR_CONSENT_ORDER, "applyingForConsentOrder");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.DIVORCE_STAGE_REACHED, "divorceStageReached");

        // Section 1 - further details of application
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.ADDRESS_OF_PROPERTIES, "natureOfApplication3a");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.MORTGAGE_DETAILS, "natureOfApplication3b");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "ChildSupportAgencyCalculationMade");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_REASON, "ChildSupportAgencyCalculationReason");

        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_NAME, "solicitorName");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_FIRM, "solicitorFirm");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_DX_NUMBER, "solicitorDXnumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_SOLICITOR_REFERENCE, "solicitorReference");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_PBA_NUMBER, "PBANumber");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_PHONE, "applicantPhone");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.APPLICANT_EMAIL, "applicantEmail");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_NAME, "authorisationName");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_FIRM, "authorisationFirm");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_SOLICITOR_ADDRESS, "authorisationSolicitorAddress");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_SIGNED_BY, "authorisationSignedBy");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.AUTHORISATION_SOLICITOR_POSITION, "authorisation2b");

        exceptionRecordToCcdFieldsMap.put(OcrFieldName.RESPONDENT_SOLICITOR_FIRM, "rSolicitorFirm");
        exceptionRecordToCcdFieldsMap.put(OcrFieldName.RESPONDENT_SOLICITOR_NAME, "rSolicitorName");

        return exceptionRecordToCcdFieldsMap;
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    @Override
    protected Map<String, Object> transformAdditionalDataFromExceptionRecord(ExceptionRecord exceptionRecord) {
        Map<String, Object> additionalCaseData = new HashMap<>();

        List<InputScannedDoc> scannedDocuments = exceptionRecord.getScannedDocuments();

        List<InputScannedDoc> inputScannedDocs = Optional.ofNullable(scannedDocuments).orElse(emptyList());

        inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals(FORM_A_DOCUMENT))
            .findFirst()
            .ifPresent(doc -> {
                additionalCaseData.put(FORMA, transformInputScannedDocIntoCaseDocument(doc));
                additionalCaseData.put(FORMA_TYPE, doc.getType());
                additionalCaseData.put(FORMA_SUBTYPE, doc.getSubtype());
                additionalCaseData.put(FORMA_CONTROL_NUMBER, doc.getControlNumber());
                additionalCaseData.put(FORMA_FILE_NAME, doc.getFileName());
                additionalCaseData.put(FORMA_SCANNED_DATE, doc.getScannedDate());
                additionalCaseData.put(FORMA_DELIVERY_DATE, doc.getDeliveryDate());
                additionalCaseData.put(FORMA_EXCEPTIONAL_RECORD_REFERENCE, exceptionRecord.getId());
            });

        ComplexTypeCollection<CaseDocument> d81DocumentCollection = inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals(D81_DOCUMENT))
            .map(this::transformInputScannedDocIntoCaseDocument)
            .collect(Collectors.collectingAndThen(toList(), ComplexTypeCollection::new));
        additionalCaseData.put("scannedD81s", d81DocumentCollection);

        ComplexTypeCollection<ScannedD81Document> scannedD81Collection = inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals(D81_DOCUMENT))
            .map(doc -> ScannedD81Document.builder()
                .documentLink(transformInputScannedDocIntoCaseDocument(doc))
                .type(ScannedDocumentType.forValue(doc.getType()))
                .subtype(doc.getSubtype())
                .controlNumber(doc.getControlNumber())
                .fileName(doc.getFileName())
                .scannedDate(doc.getScannedDate())
                .deliveryDate(doc.getDeliveryDate())
                .exceptionRecordReference(exceptionRecord.getId())
                .build()
            )
            .collect(Collectors.collectingAndThen(toList(), ComplexTypeCollection::new));
        additionalCaseData.put("scannedD81Collection", scannedD81Collection);

        additionalCaseData.put(PENSION_DOCS_COLLECTION, transformIntoTypedCaseDocuments(inputScannedDocs, Map.of(
            P1_DOCUMENT, "Form P1",
            PPF1_DOCUMENT, "Form PPF1",
            P2_DOCUMENT, "Form P2",
            PPF2_DOCUMENT, "Form PPF2",
            PPF_DOCUMENT, "Form PPF"
        )));

        additionalCaseData.put(OTHER_DOCS_COLLECTION, transformIntoTypedCaseDocuments(inputScannedDocs, Map.of(
            FORM_E_DOCUMENT, "Other",
            OTHER_SUPPORT_DOCUMENTS, "Other",
            COVER_LETTER_DOCUMENT, "Letter"
        )));

        inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals(DRAFT_CONSENT_ORDER_DOCUMENT))
            .findFirst()
            .ifPresent(doc -> {
                additionalCaseData.put(CONSENT_ORDER, transformInputScannedDocIntoCaseDocument(doc));
                additionalCaseData.put(CONSENT_ORDER_TYPE, doc.getType());
                additionalCaseData.put(CONSENT_ORDER_SUBTYPE, doc.getSubtype());
                additionalCaseData.put(CONSENT_ORDER_CONTROL_NUMBER, doc.getControlNumber());
                additionalCaseData.put(CONSENT_ORDER_FILE_NAME, doc.getFileName());
                additionalCaseData.put(CONSENT_ORDER_SCANNED_DATE, doc.getScannedDate());
                additionalCaseData.put(CONSENT_ORDER_DELIVERY_DATE, doc.getDeliveryDate());
                additionalCaseData.put(CONSENT_ORDER_EXCEPTIONAL_RECORD_REFERENCE, exceptionRecord.getId());
                additionalCaseData.put(LATEST_CONSENT_ORDER, transformInputScannedDocIntoCaseDocument(doc));
            });

        inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals(DECREE_NISI_DOCUMENT))
            .findFirst()
            .map(this::transformInputScannedDocIntoCaseDocument)
            .ifPresent(doc -> additionalCaseData.put("divorceUploadEvidence1", doc));

        inputScannedDocs.stream()
            .filter(doc -> doc.getSubtype().equals(DECREE_ABSOLUTE_DOCUMENT))
            .findFirst()
            .map(this::transformInputScannedDocIntoCaseDocument)
            .ifPresent(doc -> additionalCaseData.put("divorceUploadEvidence2", doc));

        return additionalCaseData;
    }

    private ComplexTypeCollection<TypedCaseDocument> transformIntoTypedCaseDocuments(List<InputScannedDoc> inputScannedDocs,
                                                                                     Map<String, String> subTypeToDocumentTypeMap) {
        ComplexTypeCollection<TypedCaseDocument> documentCollection = new ComplexTypeCollection<>();

        inputScannedDocs.stream()
            .filter(doc -> subTypeToDocumentTypeMap.containsKey(doc.getSubtype()))
            .map(doc -> transformInputScannedDocIntoTypedCaseDocument(subTypeToDocumentTypeMap, doc))
            .forEach(documentCollection::addItem);

        return documentCollection;
    }

    private TypedCaseDocument transformInputScannedDocIntoTypedCaseDocument(Map<String, String> subTypeToDocumentTypeMap, InputScannedDoc doc) {
        String typeOfDocument = subTypeToDocumentTypeMap.get(doc.getSubtype());
        CaseDocument caseDocument = transformInputScannedDocIntoCaseDocument(doc);

        return new TypedCaseDocument(typeOfDocument, caseDocument);
    }

    private CaseDocument transformInputScannedDocIntoCaseDocument(InputScannedDoc doc) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl(doc.getDocument().getUrl());
        caseDocument.setDocumentBinaryUrl(doc.getDocument().getBinaryUrl());
        caseDocument.setDocumentFilename(doc.getDocument().getFilename());

        return caseDocument;
    }

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {
        Map<String, Object> transformedCaseData = new HashMap<>();

        mapFullNameToFirstAndLast(OcrFieldName.APPLICANT_FULL_NAME, "applicantFMName", "applicantLName",
            ocrDataFields, transformedCaseData);
        mapFullNameToFirstAndLast(OcrFieldName.RESPONDENT_FULL_NAME, CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "appRespondentLName",
            ocrDataFields, transformedCaseData);

        commaSeparatedEntryTransformer(OcrFieldName.NATURE_OF_APPLICATION, "natureOfApplication2", natureOfApplicationChecklistToCcdFieldNames,
            ocrDataFields, transformedCaseData);
        commaSeparatedEntryTransformer(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "dischargePeriodicalPaymentSubstituteFor",
            dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames, ocrDataFields, transformedCaseData);

        ContactDetailsMapper.applyAddressesMappings(ocrDataFields, transformedCaseData);
        ChildrenInfoMapper.applyMappings(ocrDataFields, transformedCaseData);

        mapAuthorisationSignedToYesOrNo(OcrFieldName.AUTHORISATION_SIGNED, "authorisationSigned", ocrDataFields, transformedCaseData);

        mapFormDateToCcdDate(OcrFieldName.AUTHORISATION_DATE, "authorisation3", ocrDataFields, transformedCaseData);

        commaSeparatedEntryTransformer(OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT, "natureOfApplication6",
            orderForChildrenNoAgreementToCcdFieldNames, ocrDataFields, transformedCaseData);

        getValueFromOcrDataFields(OcrFieldName.ORDER_FOR_CHILDREN, ocrDataFields)
            .map(orderForChildrenToCcdFieldNames::get)
            .ifPresent(value -> transformedCaseData.put(NATURE_OF_APP_5B, value));

        getValueFromOcrDataFields(OcrFieldName.PROVISION_MADE_FOR, ocrDataFields)
            .map(provisionMadeForToCcdFieldNames::get)
            .ifPresent(value -> transformedCaseData.put("provisionMadeFor", value));

        getValueFromOcrDataFields(OcrFieldName.APPLICANT_REPRESENTED, ocrDataFields)
            .map(applicantRepresentedPaperToCcdFieldNames::get)
            .ifPresent(value -> transformedCaseData.put(APPLICANT_REPRESENTED_PAPER, value));

        return transformedCaseData;
    }

    @Override
    protected Map<String, Object> runPostMappingModification(final Map<String, Object> transformedCaseData) {
        Map<String, Object> modifiedCaseData = new HashMap<>(transformedCaseData);

        modifiedCaseData.put(PAPER_APPLICATION, YES_VALUE);
        modifiedCaseData.put(APPLICANT_REPRESENTED, getValueForIsRepresented(modifiedCaseData));
        modifiedCaseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, getSolicitorAgreeToReceiveEmailsField(modifiedCaseData));
        modifiedCaseData.put(CONSENTED_RESPONDENT_REPRESENTED, getRespondentRepresentedField(modifiedCaseData));

        // If OrderForChildren is populated then set orderForChildrenQuestion1 to Yes
        if (caseDataService.isNotEmpty(NATURE_OF_APP_5B, modifiedCaseData)) {
            modifiedCaseData.put("orderForChildrenQuestion1", YES_VALUE);
        }

        ContactDetailsMapper.setupContactDetailsForApplicantAndRespondent(modifiedCaseData);

        modifiedCaseData.replaceAll((key, value) -> (value instanceof String
            && value.toString().trim().isEmpty()) ? null : value);

        return modifiedCaseData;
    }

    private String getValueForIsRepresented(Map<String, Object> modifiedCaseData) {
        String applicantRepresentedPaperValue = CaseDataService.nullToEmpty(modifiedCaseData.get(APPLICANT_REPRESENTED_PAPER));

        return applicantRepresentedPaperValue.equalsIgnoreCase(FR_APPLICANT_REPRESENTED_3) ? YES_VALUE : NO_VALUE;
    }

    /**
     * It's correct to check if APPLICANT_EMAIL is not empty, as this method is called when email is stored only
     * in this field. In next step this may be migrated to solicitorEmail field, if applicant is represented.
     * For new version of FormA we don't store both values, but either applicant or solicitor email and only 1 value
     * is provided in list of OCR fields.
     */
    private String getSolicitorAgreeToReceiveEmailsField(Map<String, Object> modifiedCaseData) {
        return (YES_VALUE.equalsIgnoreCase(CaseDataService.nullToEmpty(modifiedCaseData.get(APPLICANT_REPRESENTED)))
            && caseDataService.isNotEmpty(APPLICANT_EMAIL, modifiedCaseData)) ? YES_VALUE : NO_VALUE;
    }

    private String getRespondentRepresentedField(Map<String, Object> modifiedCaseData) {
        return caseDataService.isNotEmpty(RESP_SOLICITOR_NAME, modifiedCaseData) ? YES_VALUE : NO_VALUE;
    }

    private void mapFormDateToCcdDate(String ocrFieldName, String ccdFieldName,
                                      List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {
        getValueFromOcrDataFields(ocrFieldName, ocrDataFields).ifPresent(ocrAuthorisationDate -> {
            String ccdAuthorisationDate = transformFormDateIntoCcdDate(OcrFieldName.AUTHORISATION_DATE, ocrAuthorisationDate);
            formSpecificMap.put(ccdFieldName, ccdAuthorisationDate);
        });
    }

    private void mapAuthorisationSignedToYesOrNo(String ocrFieldName, String ccdFieldName,
                                                 List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {
        ocrDataFields.stream()
            .filter(ocrDataField -> ocrDataField.getName().equals(ocrFieldName))
            .map(OcrDataField::getValue)
            .findFirst()
            .ifPresent(ocrValue -> {
                String ccdValue = ocrValue.trim().isEmpty() ? NO_VALUE : YES_VALUE;
                formSpecificMap.put(ccdFieldName, ccdValue);
            });
    }

    private void mapFullNameToFirstAndLast(String ocrFieldName, String ccdFirstNameFieldName, String ccdLastNameFieldName,
                                           List<OcrDataField> ocrDataFields, Map<String, Object> formSpecificMap) {

        getValueFromOcrDataFields(ocrFieldName, ocrDataFields)
            .ifPresent(fullName -> {
                List<String> nameElements = asList(fullName.split(" "));
                formSpecificMap.put(ccdFirstNameFieldName, String.join(" ", nameElements.subList(0, nameElements.size() - 1)));
                formSpecificMap.put(ccdLastNameFieldName, nameElements.get(nameElements.size() - 1));
            });
    }

    private void commaSeparatedEntryTransformer(String ocrNameWithCommaSeparatedValues,
                                                String ccdName,
                                                Map<String, String> ocrValuesToCcdValues,
                                                List<OcrDataField> ocrDataFields,
                                                Map<String, Object> transformedCaseData) {

        Optional<String> commaSeparatedOcrValue = getValueFromOcrDataFields(ocrNameWithCommaSeparatedValues, ocrDataFields);

        if (commaSeparatedOcrValue.isPresent()) {
            List<String> transformedCommaSeparatedValue =
                getCommaSeparatedValuesFromOcrDataField(commaSeparatedOcrValue.get())
                    .stream()
                    .map(ocrValuesToCcdValues::get)
                    .filter(Objects::nonNull)
                    .toList();

            if (!transformedCommaSeparatedValue.isEmpty()) {
                transformedCaseData.put(ccdName, transformedCommaSeparatedValue);
            }
        }
    }
}
