package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CreateGeneralLetterDocumentCategoriser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterService {

    private final GenericDocumentService genericDocumentService;
    private final BulkPrintService bulkPrintService;
    private final BulkPrintDocumentService bulkPrintDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final CaseDataService caseDataService;
    private final CreateGeneralLetterDocumentCategoriser createGeneralLetterDocumentCategoriser;
    private final InternationalPostalService postalService;

    public void previewGeneralLetter(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General letter preview for Case ID: {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        addFrcCourtFields(caseData);
        CaseDocument generalLetterDocument = generateGeneralLetterDocument(caseDetails, authorisationToken);
        caseData.getGeneralLetterWrapper().setGeneralLetterPreview(generalLetterDocument);
        removeFrcCourtFields(caseData);
    }

    public void createGeneralLetter(String authorisationToken, FinremCaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        addFrcCourtFields(caseData);
        GeneralLetterWrapper wrapper = caseData.getGeneralLetterWrapper();
        log.info("Generating General letter for Case ID: {}", caseId);
        CaseDocument document = generateGeneralLetterDocument(caseDetails, authorisationToken);
        List<DocumentCollectionItem> pdfGeneralLetterUploadedDocuments = getUploadedDocumentsAsPdfs(authorisationToken, wrapper, caseId);

        addGeneralLetterToCaseData(caseDetails, document, pdfGeneralLetterUploadedDocuments);
        printLatestGeneralLetter(caseDetails, authorisationToken);
        removeFrcCourtFields(caseData);
        if (caseData.isContestedApplication()) {
            createGeneralLetterDocumentCategoriser.categorise(caseData);
        }
    }

    private List<DocumentCollectionItem> getUploadedDocumentsAsPdfs(String authorisationToken, GeneralLetterWrapper wrapper, Long caseId) {
        Optional<List<DocumentCollectionItem>> generalLetterUploadedDocuments = Optional.ofNullable(wrapper.getGeneralLetterUploadedDocuments());

        List<DocumentCollectionItem> pdfGeneralLetterUploadedDocuments = new ArrayList<>();

        generalLetterUploadedDocuments.ifPresent(uploadedDocuments -> {
            if (!uploadedDocuments.isEmpty()) {
                uploadedDocuments.forEach(generalLetterUploadedDocumentCollection -> {
                    CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                        generalLetterUploadedDocumentCollection.getValue(),
                        authorisationToken, caseId.toString());
                    pdfGeneralLetterUploadedDocuments.add(DocumentCollectionItem.builder().value(pdfDocument).build());
                });
                wrapper.setGeneralLetterUploadedDocuments(pdfGeneralLetterUploadedDocuments);
            }
        });
        return pdfGeneralLetterUploadedDocuments;
    }

    private CaseDocument generateGeneralLetterDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDetails templateCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(templateCaseDetails, CaseDetails.class);
        prepareCaseDetailsForDocumentGeneration(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            getGeneralLetterTemplate(caseDetails.getData()), documentConfiguration.getGeneralLetterFileName());
    }

    public void validateEncryptionOnUploadedDocuments(List<DocumentCollectionItem> caseDocuments, String caseId,
                                                      String auth, List<String> errors) {
        caseDocuments.forEach(doc -> {
            if (doc != null && doc.getValue() != null) {
                bulkPrintDocumentService.validateEncryptionOnUploadedDocument(
                    doc.getValue(), caseId, errors, auth);
            }
        });
    }

    private String getGeneralLetterTemplate(FinremCaseData caseData) {
        return caseData.isContestedApplication() ? documentConfiguration.getContestedGeneralLetterTemplate()
            : documentConfiguration.getConsentGeneralLetterTemplate();
    }

    private void prepareCaseDetailsForDocumentGeneration(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("generalLetterCreatedDate", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()));
        caseData.put("ccdCaseNumber", caseDetails.getId());
        caseData.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        caseData.put("applicantFullName", caseDataService.buildFullApplicantName(caseDetails));
        caseData.put("respondentFullName", caseDataService.buildFullRespondentName(caseDetails));
        populateNameAddressAndReference(caseDetails);
    }

    private String getIntervenerAddressee(IntervenerWrapper wrapper, String generalLetterAddressee) {
        return generalLetterAddressee.equals(INTERVENER1) || generalLetterAddressee.equals(INTERVENER2)
               || generalLetterAddressee.equals(INTERVENER3) || generalLetterAddressee.equals(INTERVENER4)
               ? wrapper.getIntervenerName() : wrapper.getIntervenerSolName();
    }

    private void addGeneralLetterToCaseData(FinremCaseDetails caseDetails, CaseDocument document,
                                            List<DocumentCollectionItem> generalLetterUploadedDocuments) {
        List<GeneralLetterCollection> generalLetterCollection = Optional.ofNullable(caseDetails.getData()
            .getGeneralLetterWrapper().getGeneralLetterCollection())
            .orElse(new ArrayList<>(1));
        generalLetterCollection.add(GeneralLetterCollection.builder().value(GeneralLetter.builder()
                .generatedLetter(document)
                .generalLetterUploadedDocuments(generalLetterUploadedDocuments)
                .build())
            .build());
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterCollection(generalLetterCollection);
    }

    public List<String> getCaseDataErrorsForCreatingPreviewOrFinalLetter(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        String letterAddressToType = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();
        Address recipientAddress = getRecipientAddress(caseDetails);

        if (recipientAddress == null || StringUtils.isEmpty(recipientAddress.getPostCode())) {
            return Collections.singletonList(String.format("Address is missing for recipient type %s", letterAddressToType));
        } else {
            return emptyList();
        }
    }

    private void populateNameAddressAndReference(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        String addressee = finremCaseDetails.getData().getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();
        boolean recipientResideOutsideOfUK = postalService.isRecipientResideOutsideOfUK(finremCaseDetails.getData(), addressee);
        Addressee generalLetterAddressee = Addressee.builder().name(getRecipientName(finremCaseDetails))
                .formattedAddress(formatAddressForLetterPrinting(getRecipientAddress(finremCaseDetails), recipientResideOutsideOfUK)).build();
        String reference = getRecipientSolicitorReference(finremCaseDetails);
        data.put(ADDRESSEE, generalLetterAddressee);
        data.put("reference", reference);
    }

    private String getRecipientSolicitorReference(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        ContactDetailsWrapper wrapper = data.getContactDetailsWrapper();
        IntervenerOne intervenerOne = data.getIntervenerOne();
        IntervenerTwo intervenerTwo = data.getIntervenerTwo();
        IntervenerThree intervenerThree = data.getIntervenerThree();
        IntervenerFour intervenerFour = data.getIntervenerFour();
        String letterAddresseeType = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();
        return switch (letterAddresseeType) {
            case APPLICANT_SOLICITOR -> wrapper.getSolicitorReference();
            case RESPONDENT_SOLICITOR -> wrapper.getRespondentSolicitorReference();
            case INTERVENER1_SOLICITOR -> intervenerOne.getIntervenerSolicitorReference();
            case INTERVENER2_SOLICITOR -> intervenerTwo.getIntervenerSolicitorReference();
            case INTERVENER3_SOLICITOR -> intervenerThree.getIntervenerSolicitorReference();
            case INTERVENER4_SOLICITOR -> intervenerFour.getIntervenerSolicitorReference();
            default -> null;
        };
    }

    private String getRecipientName(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        String generalLetterAddressee = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();

        return switch (generalLetterAddressee) {
            case APPLICANT_SOLICITOR -> data.getAppSolicitorName();
            case RESPONDENT_SOLICITOR -> data.getRespondentSolicitorName();
            case RESPONDENT -> data.getRespondentFullName();
            case APPLICANT -> data.getFullApplicantName();
            case OTHER_RECIPIENT -> data.getGeneralLetterWrapper().getGeneralLetterRecipient();
            case INTERVENER1, INTERVENER1_SOLICITOR -> getIntervenerAddressee(data.getIntervenerOne(), generalLetterAddressee);
            case INTERVENER2, INTERVENER2_SOLICITOR -> getIntervenerAddressee(data.getIntervenerTwo(), generalLetterAddressee);
            case INTERVENER3, INTERVENER3_SOLICITOR -> getIntervenerAddressee(data.getIntervenerThree(), generalLetterAddressee);
            case INTERVENER4, INTERVENER4_SOLICITOR -> getIntervenerAddressee(data.getIntervenerFour(), generalLetterAddressee);
            default -> null;
        };
    }

    private Address getRecipientAddress(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        String letterAddresseeType = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();

        return switch (letterAddresseeType) {
            case APPLICANT_SOLICITOR -> data.getAppSolicitorAddress();
            case RESPONDENT_SOLICITOR -> data.getContactDetailsWrapper().getRespondentSolicitorAddress();
            case RESPONDENT -> data.getContactDetailsWrapper().getRespondentAddress();
            case APPLICANT -> data.getContactDetailsWrapper().getApplicantAddress();
            case OTHER_RECIPIENT -> data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress();
            case INTERVENER1, INTERVENER1_SOLICITOR -> data.getIntervenerOne().getIntervenerAddress();
            case INTERVENER2, INTERVENER2_SOLICITOR -> data.getIntervenerTwo().getIntervenerAddress();
            case INTERVENER3, INTERVENER3_SOLICITOR -> data.getIntervenerThree().getIntervenerAddress();
            case INTERVENER4, INTERVENER4_SOLICITOR -> data.getIntervenerFour().getIntervenerAddress();
            default -> null;
        };
    }

    private UUID printLatestGeneralLetter(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        GeneralLetterWrapper generalLetterWrapper = caseDetails.getData().getGeneralLetterWrapper();
        List<GeneralLetterCollection> generalLettersData = generalLetterWrapper.getGeneralLetterCollection();
        GeneralLetterCollection latestGeneralLetterData = generalLettersData.get(generalLettersData.size() - 1);
        bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(latestGeneralLetterData.getValue().getGeneratedLetter()));
        Optional.ofNullable(generalLetterWrapper.getGeneralLetterUploadedDocument())
            .ifPresent(uploadedDocument -> bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(uploadedDocument)));

        Optional.ofNullable(generalLetterWrapper.getGeneralLetterUploadedDocuments())
            .ifPresent(uploadedDocuments -> uploadedDocuments.forEach(
                generalLetterUploadedDocumentCollection -> bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(
                    generalLetterUploadedDocumentCollection.getValue())
                )
            ));
        String recipient = generalLetterWrapper.getGeneralLetterAddressee().getValue().getCode();
        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(),
            recipient,
            bulkPrintDocuments,
            postalService.isRecipientResideOutsideOfUK(caseDetails.getData(), recipient),
            authorisationToken);
    }

    private String formatAddressForLetterPrinting(Address address, boolean isInternational) {
        return formatAddressForLetterPrinting(new ObjectMapper().convertValue(address, Map.class), isInternational);
    }

    private String formatAddressForLetterPrinting(Map<String, Object> address, boolean isInternational) {
        if (address != null) {
            Stream<String> addressLines = Stream.of("AddressLine1", "AddressLine2", "AddressLine3",
                "County", "PostTown", "PostCode", isInternational ? "Country" : "");
            return addressLines.map(address::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::isNotEmpty)
                .filter(s -> !s.equals("null"))
                .collect(Collectors.joining("\n"));
        }
        return "";
    }

    private void addFrcCourtFields(FinremCaseData data) {
        data.setCourtDetails(buildFrcCourtDetails(data));
    }

    private void removeFrcCourtFields(FinremCaseData data) {
        data.setCourtDetails(null);
    }
}
