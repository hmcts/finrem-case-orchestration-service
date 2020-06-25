package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.buildCtscContactDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_PREVIEW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    public void previewGeneralLetter(String authorisationToken, CaseDetails caseDetails) {
        log.info("Generating General letter preview for Case ID: {}", caseDetails.getId());
        CaseDocument generalLetterDocument = generateGeneralLetterDocument(caseDetails, authorisationToken);
        caseDetails.getData().put(GENERAL_LETTER_PREVIEW, generalLetterDocument);
        log.info("Debugging, general letter preview {}", caseDetails.getData().get(GENERAL_LETTER_PREVIEW));
    }

    public void createGeneralLetter(String authorisationToken, CaseDetails caseDetails) {
        log.info("Generating General letter for Case ID: {}", caseDetails.getId());
        CaseDocument document = generateGeneralLetterDocument(caseDetails, authorisationToken);
        addGeneralLetterToCaseData(caseDetails, document);
    }

    private CaseDocument generateGeneralLetterDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareCaseDetailsForDocumentGeneration(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralLetterTemplate(), documentConfiguration.getGeneralLetterFileName());
    }

    private void prepareCaseDetailsForDocumentGeneration(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("generalLetterCreatedDate", new Date());
        caseData.put("ccdCaseNumber", caseDetails.getId());
        caseData.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        populateNameAddressAndReference(caseDetails);
    }

    private void populateNameAddressAndReference(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        String generalLetterAddressTo = (String) data.get(GENERAL_LETTER_ADDRESS_TO);
        boolean isConsentedApplication = isConsentedApplication(caseDetails);

        log.info("Debugging, isConsentedApplication {}", isConsentedApplication);

        Addressee.AddresseeBuilder addresseeBuilder = Addressee.builder();
        if ("applicantSolicitor".equalsIgnoreCase(generalLetterAddressTo)) {
            data.put("reference", data.get(SOLICITOR_REFERENCE));
            String solicitorNameCcdField = isConsentedApplication ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME;
            String solicitorAddressCcdField = isConsentedApplication ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
            log.info("Debugging, solicitorNameCcdField {}, solicitorAddressCcdField {}", solicitorNameCcdField, solicitorAddressCcdField);
            log.info("Debugging, name {}", data.get(solicitorNameCcdField));
            log.info("Debugging, formatted address {}", documentHelper.formatAddressForLetterPrinting((Map) data.get(solicitorAddressCcdField)));
            addresseeBuilder
                .name((String) data.get(solicitorNameCcdField))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(solicitorAddressCcdField)));
        } else if ("respondentSolicitor".equalsIgnoreCase(generalLetterAddressTo)) {
            data.put("reference", data.get("rSolicitorReference"));
            addresseeBuilder
                .name((String) data.get(RESP_SOLICITOR_NAME))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(RESP_SOLICITOR_ADDRESS)));
        } else if ("respondent".equalsIgnoreCase(generalLetterAddressTo)) {
            String respondentFmNameCcdField = isConsentedApplication ? APP_RESPONDENT_FIRST_MIDDLE_NAME : "respondentFMName";
            String respondentLastNameCcdField = isConsentedApplication ? APP_RESPONDENT_LAST_NAME : "respondentLName";
            log.info("Debugging, respondentFmNameCcdField {}, respondentLastNameCcdField {}", respondentFmNameCcdField, respondentLastNameCcdField);
            addresseeBuilder
                .name(StringUtils.joinWith(" ", data.get(respondentFmNameCcdField), data.get(respondentLastNameCcdField)))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get(RESPONDENT_ADDRESS)));
        } else if ("other".equalsIgnoreCase(generalLetterAddressTo)) {
            addresseeBuilder
                .name((String) data.get(GENERAL_LETTER_RECIPIENT))
                .formattedAddress(documentHelper.formatAddressForLetterPrinting((Map) data.get("generalLetterRecipientAddress")));
        }
        data.put(ADDRESSEE, addresseeBuilder.build());
        log.info("Debugging, addressee {}", data.get(ADDRESSEE));
    }

    private void addGeneralLetterToCaseData(CaseDetails caseDetails, CaseDocument document) {
        GeneralLetterData generatedLetterData = GeneralLetterData.builder()
            .generalLetter(GeneralLetter.builder()
                .generatedLetter(document)
                .build())
            .build();

        Map<String, Object> caseData = caseDetails.getData();
        List<GeneralLetterData> generalLetterDataList = Optional.ofNullable(caseData.get(GENERAL_LETTER))
            .map(this::convertToGeneralLetterData)
            .orElse(new ArrayList<>(1));

        generalLetterDataList.add(generatedLetterData);

        caseData.put(GENERAL_LETTER, generalLetterDataList);
    }

    private List<GeneralLetterData> convertToGeneralLetterData(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralLetterData>>() {
        });
    }

    public List<String> getCaseDataErrorsForCreatingPreviewOrFinalLetter(CaseDetails caseDetails) {
        boolean isConsentedApplication = isConsentedApplication(caseDetails);
        Map<String, String> generalLetterAddressToValueToAddressCcdFieldName = ImmutableMap.of(
            "applicantSolicitor", isConsentedApplication ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS,
            "respondentSolicitor", RESP_SOLICITOR_ADDRESS,
            "respondent", RESPONDENT_ADDRESS,
            "other", "generalLetterRecipientAddress");

        Map<String, Object> data = caseDetails.getData();
        String generalLetterAddressTo = (String) data.get(GENERAL_LETTER_ADDRESS_TO);
        Map<String, Object> recipientAddress = (Map) data.get(generalLetterAddressToValueToAddressCcdFieldName.get(generalLetterAddressTo));
        if (recipientAddress == null || recipientAddress.isEmpty()) {
            return asList(String.format("Address is missing for recipient type %s", generalLetterAddressTo));
        } else {
            return emptyList();
        }
    }
}
