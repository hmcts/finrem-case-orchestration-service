package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DataForTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.HelpWithFeesSuccessLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.TemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AbstractDocumentService;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.LetterAddressHelper.formatAddressForLetterPrinting;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
public class HelpWithFeesBulkPrintService extends AbstractDocumentService {

    @Autowired
    public HelpWithFeesBulkPrintService(DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDetails sendLetter(String authToken, CaseDetails caseDetails) {
        log.info("HWF success bulk print letter - started");

        CaseDocument successHwFLetter = generateHwFBulkPrintLetter(caseDetails, authToken);

        log.info("HWF success bulk print letter - generated {}", successHwFLetter);

        caseDetails.getData().put("hwfSuccessNotificationLetter", successHwFLetter);

        return caseDetails;
    }

    private CaseDocument generateHwFBulkPrintLetter(CaseDetails caseDetails, String authToken) {
        String template = config.getHelpWithFeesSuccessfulTemplate();
        String filename = config.getHelpWithFeesSuccessfulFileName();

        log.info("Generating Help with Fees success Letter {} from {} for bulk print", template, filename);

        try {
            DataForTemplate data = prepareHelpWithFeesSuccessLetter(caseDetails);
            return generateDocument(authToken, data, new TemplateDetails(template, filename));
        } catch (IllegalArgumentException exception) {
            log.warn("Failed to generate Help with Fees Success Letter as not all required address details were present");
            throw exception;
        }
    }

    private HelpWithFeesSuccessLetter prepareHelpWithFeesSuccessLetter(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        HelpWithFeesSuccessLetter.HelpWithFeesSuccessLetterBuilder hwfBuilder = HelpWithFeesSuccessLetter.builder();
        Addressee.AddresseeBuilder addresseeBuilder = Addressee.builder();
        String applicantName = buildFullName(caseData, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);

        hwfBuilder
                .caseNumber(nullToEmpty((caseDetails.getId())))
                .reference("");

        if (isApplicantRepresented(caseData)) {
            log.info("Applicant is represented by a solicitor");
            addresseeBuilder
                    .name(nullToEmpty(caseData.get(SOLICITOR_NAME)))
                    .formattedAddress(formatAddressForLetterPrinting((Map) caseData.get(APP_SOLICITOR_ADDRESS_CCD_FIELD)));
            hwfBuilder
                    .applicantName(applicantName)
                    .reference(nullToEmpty(caseData.get(SOLICITOR_REFERENCE)));
        } else {
            log.info("Applicant is not represented by a solicitor");
            addresseeBuilder
                    .name(applicantName)
                    .formattedAddress(formatAddressForLetterPrinting((Map) caseData.get(APPLICANT_ADDRESS)));
            hwfBuilder
                    .applicantName(applicantName);
        }

        return hwfBuilder
                .addressee(addresseeBuilder.build())
                .respondentName(buildFullName(caseData, APP_RESPONDENT_FIRST_MIDDLE_NAME, APP_RESPONDENT_LAST_NAME))
                .letterDate(String.valueOf(LocalDate.now()))
                .build();
    }
}
