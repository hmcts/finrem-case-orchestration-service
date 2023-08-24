package uk.gov.hmcts.reform.finrem.caseorchestration.service;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentedContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContestedContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_RECIPIENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterService {

    private final GenericDocumentService genericDocumentService;
    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

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

    private void populateNameAddressAndReference(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        String generalLetterAddressTo = (String) data.get(GENERAL_LETTER_ADDRESS_TO);
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);

        Addressee.AddresseeBuilder addresseeBuilder = Addressee.builder();
        if ("applicantSolicitor".equalsIgnoreCase(generalLetterAddressTo)) {
            data.put("reference", data.get(SOLICITOR_REFERENCE));
            String solicitorNameCcdField = isConsentedApplication ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME;
            String solicitorAddressCcdField = isConsentedApplication ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
            addresseeBuilder
                .name((String) data.get(solicitorNameCcdField))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(solicitorAddressCcdField)));
        } else if ("respondentSolicitor".equalsIgnoreCase(generalLetterAddressTo)) {
            data.put("reference", data.get("rSolicitorReference"));
            addresseeBuilder
                .name((String) data.get(RESP_SOLICITOR_NAME))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(RESP_SOLICITOR_ADDRESS)));
        } else if ("respondent".equalsIgnoreCase(generalLetterAddressTo)) {
            String respondentFmNameCcdField =
                isConsentedApplication ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
            String respondentLastNameCcdField = isConsentedApplication ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME;
            addresseeBuilder
                .name(StringUtils.joinWith(" ", data.get(respondentFmNameCcdField), data.get(respondentLastNameCcdField)))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(RESPONDENT_ADDRESS)));
        } else if ("applicant".equalsIgnoreCase(generalLetterAddressTo)) {
            String applicantFmNameCcdField = APPLICANT_FIRST_MIDDLE_NAME;
            String applicantLastNameCcdField = APPLICANT_LAST_NAME;
            addresseeBuilder
                .name(StringUtils.joinWith(" ", data.get(applicantFmNameCcdField), data.get(applicantLastNameCcdField)))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(APPLICANT_ADDRESS)));
        } else if ("other".equalsIgnoreCase(generalLetterAddressTo)) {
            addresseeBuilder
                .name((String) data.get(GENERAL_LETTER_RECIPIENT))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(GENERAL_LETTER_RECIPIENT_ADDRESS)));
        }
        data.put("recipient", generalLetterAddressTo);
        data.put(ADDRESSEE, addresseeBuilder.build());
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
        GeneralLetterAddressToType letterAddressToType = data.getGeneralLetterWrapper().getGeneralLetterAddressTo();
        Address recipientAddress = getRecipientAddress(caseDetails);

        if (recipientAddress == null || StringUtils.isEmpty(recipientAddress.getPostCode())) {
            return Collections.singletonList(String.format("Address is missing for recipient type %s", letterAddressToType.getValue()));
        } else {
            return emptyList();
        }
    }

    private Address getRecipientAddress(FinremCaseDetails caseDetails) {
        Address recipientAddress;
        FinremCaseData data = caseDetails.getData();
        GeneralLetterAddressToType letterAddressToType = data.getGeneralLetterWrapper().getGeneralLetterAddressTo();

        switch (letterAddressToType) {
            case APPLICANT_SOLICITOR:
                recipientAddress = caseDetails.getData().isConsentedApplication()
                    ? ((ConsentedContactDetailsWrapper) data.getContactDetailsWrapper()).getSolicitorAddress()
                    : ((ContestedContactDetailsWrapper) data.getContactDetailsWrapper()).getApplicantSolicitorAddress();
                break;
            case RESPONDENT_SOLICITOR:
                recipientAddress = data.getContactDetailsWrapper().getRespondentSolicitorAddress();
                break;
            case RESPONDENT:
                recipientAddress = data.getContactDetailsWrapper().getRespondentAddress();
                break;
            case APPLICANT:
                recipientAddress = data.getContactDetailsWrapper().getApplicantAddress();
                break;
            case OTHER:
                recipientAddress = data.getGeneralLetterWrapper().getGeneralLetterRecipientAddress();
                break;
            default:
                recipientAddress = null;
        }
        return recipientAddress;
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
}
