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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService.OTHER;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterService {

    private final GenericDocumentService genericDocumentService;
    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final CaseDataService caseDataService;

    public void previewGeneralLetter(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General letter preview for Case ID: {}", caseDetails.getId());
        CaseDocument generalLetterDocument = generateGeneralLetterDocument(caseDetails, authorisationToken);
        caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterPreview(generalLetterDocument);
    }

    public void createGeneralLetter(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating General letter for Case ID: {}", caseDetails.getId());
        CaseDocument document = generateGeneralLetterDocument(caseDetails, authorisationToken);
        CaseDocument generalLetterUploadedDocument = caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterUploadedDocument();
        if (generalLetterUploadedDocument != null) {
            CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(generalLetterUploadedDocument,
                authorisationToken, caseDetails.getId().toString());
            caseDetails.getData().getGeneralLetterWrapper().setGeneralLetterUploadedDocument(pdfDocument);
        }
        addGeneralLetterToCaseData(caseDetails, document,
            caseDetails.getData().getGeneralLetterWrapper().getGeneralLetterUploadedDocument());
        printLatestGeneralLetter(caseDetails, authorisationToken);
    }

    private CaseDocument generateGeneralLetterDocument(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDetails templateCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(templateCaseDetails, CaseDetails.class);
        prepareCaseDetailsForDocumentGeneration(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralLetterTemplate(), documentConfiguration.getGeneralLetterFileName());
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
                                            CaseDocument generalLetterUploadedDocument) {
        List<GeneralLetterCollection> generalLetterCollection = Optional.ofNullable(caseDetails.getData()
            .getGeneralLetterWrapper().getGeneralLetterCollection())
            .orElse(new ArrayList<>(1));
        generalLetterCollection.add(GeneralLetterCollection.builder().value(GeneralLetter.builder()
                .generatedLetter(document)
                .generalLetterUploadedDocument(generalLetterUploadedDocument)
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
        Addressee generalLetterAddressee = Addressee.builder().name(getRecipientName(finremCaseDetails))
                .formattedAddress(formatAddressForLetterPrinting(getRecipientAddress(finremCaseDetails))).build();
        String reference = getRecipientSolicitorReference(finremCaseDetails);
        data.put(ADDRESSEE, generalLetterAddressee);
        data.put("reference", reference);
    }

    private String getRecipientSolicitorReference(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        ContactDetailsWrapper wrapper = data.getContactDetailsWrapper();
        IntervenerOneWrapper intervenerOneWrapper = data.getIntervenerOneWrapper();
        IntervenerTwoWrapper intervenerTwoWrapper = data.getIntervenerTwoWrapper();
        IntervenerThreeWrapper intervenerThreeWrapper = data.getIntervenerThreeWrapper();
        IntervenerFourWrapper intervenerFourWrapper = data.getIntervenerFourWrapper();
        String letterAddresseeType = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();
        return switch (letterAddresseeType) {
            case APPLICANT_SOLICITOR -> wrapper.getSolicitorReference();
            case RESPONDENT_SOLICITOR -> wrapper.getRespondentSolicitorReference();
            case INTERVENER1_SOLICITOR -> intervenerOneWrapper.getIntervenerSolicitorReference();
            case INTERVENER2_SOLICITOR -> intervenerTwoWrapper.getIntervenerSolicitorReference();
            case INTERVENER3_SOLICITOR -> intervenerThreeWrapper.getIntervenerSolicitorReference();
            case INTERVENER4_SOLICITOR -> intervenerFourWrapper.getIntervenerSolicitorReference();
            default -> null;
        };
    }

    private String getRecipientName(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        String generalLetterAddressee = data.getGeneralLetterWrapper().getGeneralLetterAddressee().getValue().getCode();
        ContactDetailsWrapper wrapper = data.getContactDetailsWrapper();

        return switch (generalLetterAddressee) {
            case APPLICANT_SOLICITOR -> data.getAppSolicitorName();
            case RESPONDENT_SOLICITOR -> data.getRespondentSolicitorName();
            case RESPONDENT -> StringUtils.joinWith(" ", wrapper.getRespondentFmName(), wrapper.getRespondentLname());
            case APPLICANT -> StringUtils.joinWith(" ", wrapper.getApplicantFmName(), wrapper.getApplicantLname());
            case OTHER -> data.getGeneralLetterWrapper().getGeneralLetterRecipient();
            case INTERVENER1, INTERVENER1_SOLICITOR -> getIntervenerAddressee(data.getIntervenerOneWrapper(), generalLetterAddressee);
            case INTERVENER2, INTERVENER2_SOLICITOR -> getIntervenerAddressee(data.getIntervenerTwoWrapper(), generalLetterAddressee);
            case INTERVENER3, INTERVENER3_SOLICITOR -> getIntervenerAddressee(data.getIntervenerThreeWrapper(), generalLetterAddressee);
            case INTERVENER4, INTERVENER4_SOLICITOR -> getIntervenerAddressee(data.getIntervenerFourWrapper(), generalLetterAddressee);
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
            case OTHER -> data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress();
            case INTERVENER1, INTERVENER1_SOLICITOR -> data.getIntervenerOneWrapper().getIntervenerAddress();
            case INTERVENER2, INTERVENER2_SOLICITOR -> data.getIntervenerTwoWrapper().getIntervenerAddress();
            case INTERVENER3, INTERVENER3_SOLICITOR -> data.getIntervenerThreeWrapper().getIntervenerAddress();
            case INTERVENER4, INTERVENER4_SOLICITOR -> data.getIntervenerFourWrapper().getIntervenerAddress();
            default -> null;
        };
    }

    private UUID printLatestGeneralLetter(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        GeneralLetterWrapper generalLetterWrapper = caseDetails.getData().getGeneralLetterWrapper();
        List<GeneralLetterCollection> generalLettersData = generalLetterWrapper.getGeneralLetterCollection();
        GeneralLetterCollection latestGeneralLetterData = generalLettersData.get(generalLettersData.size() - 1);
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(latestGeneralLetterData.getValue().getGeneratedLetter()));
        CaseDocument generalLetterUploadedDocument = generalLetterWrapper.getGeneralLetterUploadedDocument();
        if (generalLetterUploadedDocument != null) {
            bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(generalLetterUploadedDocument));
        }
        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(),
            generalLetterWrapper.getGeneralLetterRecipient(),
            bulkPrintDocuments, authorisationToken);
    }

    public static String formatAddressForLetterPrinting(Address address) {
        return formatAddressForLetterPrinting(new ObjectMapper().convertValue(address, Map.class));
    }

    private static String formatAddressForLetterPrinting(Map<String, Object> address) {
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
}
