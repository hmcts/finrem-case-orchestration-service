package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContestedContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAddedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAddedSolicitorLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerRemovedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerRemovedSolicitorLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator.LETTER_DATE_FORMAT;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final CourtDetailsMapper courtDetailsMapper;

    public CaseDocument generateIntervenerAddedNotificationLetter(FinremCaseDetails finremCaseDetails, String authToken,
                                                                  DocumentHelper.PaperNotificationRecipient recipient) {


        log.info("Generating Intervener Added Notification Letter {} from {} for bulk print for {}, case id: {}",
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename(),
            recipient, finremCaseDetails.getId());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, recipient);
        finremCaseDetails.getData().setCurrentAddressee((Addressee) caseDetailsForBulkPrint.getData().get(ADDRESSEE));
        IntervenerAddedLetterDetails intervenerAddedLetterDetails = generateAddedLetterDetails(finremCaseDetails, recipient);

        return getCaseDocument(authToken, intervenerAddedLetterDetails);
    }

    public CaseDocument generateIntervenerRemovedNotificationLetter(FinremCaseDetails finremCaseDetails, String authToken,
                                                                    DocumentHelper.PaperNotificationRecipient recipient) {


        log.info("Generating Intervener Removed Notification Letter {} from {} for bulk print for {}, case id: {}",
            documentConfiguration.getIntervenerRemovedTemplate(),
            documentConfiguration.getIntervenerRemovedFilename(),
            recipient, finremCaseDetails.getId());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, recipient);
        finremCaseDetails.getData().setCurrentAddressee((Addressee) caseDetailsForBulkPrint.getData().get(ADDRESSEE));
        IntervenerRemovedLetterDetails intervenerRemovedLetterDetails = generateRemovedLetterDetails(finremCaseDetails, recipient);

        return getCaseDocument(authToken, intervenerRemovedLetterDetails);
    }

    public CaseDocument generateIntervenerSolicitorAddedLetter(FinremCaseDetails finremCaseDetails, String authToken,
                                                               DocumentHelper.PaperNotificationRecipient recipient) {

        log.info("Generating Intervener Added Solicitor Notification Letter {} from {} for bulk print for {}, case id: {}",
            documentConfiguration.getIntervenerAddedSolicitorTemplate(),
            documentConfiguration.getIntervenerAddedSolicitorFilename(),
            recipient, finremCaseDetails.getId());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, recipient);
        finremCaseDetails.getData().setCurrentAddressee((Addressee) caseDetailsForBulkPrint.getData().get(ADDRESSEE));
        IntervenerAddedSolicitorLetterDetails intervenerAddedSolicitorLetterDetails = generateSolAddedLetterDetails(finremCaseDetails, recipient);

        return getCaseDocument(authToken, intervenerAddedSolicitorLetterDetails);
    }

    public CaseDocument generateIntervenerSolicitorRemovedLetter(FinremCaseDetails finremCaseDetails, String authToken,
                                                                 DocumentHelper.PaperNotificationRecipient recipient) {

        log.info("Generating Intervener Removed Solicitor Notification Letter {} from {} for bulk print for {}, case id: {}",
            documentConfiguration.getIntervenerRemovedSolicitorTemplate(),
            documentConfiguration.getIntervenerRemovedSolicitorFilename(),
            recipient, finremCaseDetails.getId());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, recipient);
        finremCaseDetails.getData().setCurrentAddressee((Addressee) caseDetailsForBulkPrint.getData().get(ADDRESSEE));
        IntervenerRemovedSolicitorLetterDetails intervenerRemovedSolicitorLetterDetails =
            generateSolRemovedLetterDetails(finremCaseDetails, recipient);

        return getCaseDocument(authToken, intervenerRemovedSolicitorLetterDetails);
    }

    private CaseDocument getCaseDocument(String authToken, IntervenerAddedLetterDetails intervenerAddedLetterDetails) {

        CaseDocument generatedIntervenerAddedNotificationLetter = generateDocument(authToken,
            intervenerAddedLetterDetails,
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename());

        log.info("Generated Intervener Added Notification Letter: {} for case: {}",
            generatedIntervenerAddedNotificationLetter, intervenerAddedLetterDetails.getCaseNumber());
        return generatedIntervenerAddedNotificationLetter;
    }

    private CaseDocument getCaseDocument(String authToken, IntervenerRemovedLetterDetails intervenerRemovedLetterDetails) {

        CaseDocument generatedIntervenerRemovedNotificationLetter = generateDocument(authToken,
            intervenerRemovedLetterDetails,
            documentConfiguration.getIntervenerRemovedTemplate(),
            documentConfiguration.getIntervenerRemovedFilename());

        log.info("Generated Intervener Removed Notification Letter: {} for case: {}",
            generatedIntervenerRemovedNotificationLetter, intervenerRemovedLetterDetails.getCaseNumber());
        return generatedIntervenerRemovedNotificationLetter;
    }

    private CaseDocument getCaseDocument(String authToken, IntervenerAddedSolicitorLetterDetails intervenerAddedSolicitorLetterDetails) {

        CaseDocument generatedIntervenerAddedSolicitorNotificationLetter = generateDocument(authToken,
            intervenerAddedSolicitorLetterDetails,
            documentConfiguration.getIntervenerAddedSolicitorTemplate(),
            documentConfiguration.getIntervenerAddedSolicitorFilename());

        log.info("Generated Intervener Added Solicitor Notification Letter: {} for case: {}",
            generatedIntervenerAddedSolicitorNotificationLetter, intervenerAddedSolicitorLetterDetails.getCaseNumber());
        return generatedIntervenerAddedSolicitorNotificationLetter;
    }

    private CaseDocument getCaseDocument(String authToken, IntervenerRemovedSolicitorLetterDetails intervenerRemovedSolicitorLetterDetails) {

        CaseDocument generatedIntervenerRemovedSolicitorNotificationLetter = generateDocument(authToken,
            intervenerRemovedSolicitorLetterDetails,
            documentConfiguration.getIntervenerRemovedSolicitorTemplate(),
            documentConfiguration.getIntervenerRemovedSolicitorFilename());

        log.info("Generated Intervener Removed Solicitor Notification Letter: {} for case: {}",
            generatedIntervenerRemovedSolicitorNotificationLetter, intervenerRemovedSolicitorLetterDetails.getCaseNumber());
        return generatedIntervenerRemovedSolicitorNotificationLetter;
    }

    private CaseDocument generateDocument(String authToken,
                                          IntervenerAddedLetterDetails intervenerAddedLetterDetails,
                                          String template,
                                          String filename) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            convertLetterDetailsToMap(intervenerAddedLetterDetails),
            template,
            filename,
            intervenerAddedLetterDetails.getCaseNumber());
    }

    private CaseDocument generateDocument(String authToken,
                                          IntervenerRemovedLetterDetails intervenerRemovedLetterDetails,
                                          String template,
                                          String filename) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            convertLetterDetailsToMap(intervenerRemovedLetterDetails),
            template,
            filename,
            intervenerRemovedLetterDetails.getCaseNumber());
    }

    private CaseDocument generateDocument(String authToken,
                                          IntervenerAddedSolicitorLetterDetails intervenerAddedSolicitorLetterDetails,
                                          String template,
                                          String filename) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            convertLetterDetailsToMap(intervenerAddedSolicitorLetterDetails),
            template,
            filename,
            intervenerAddedSolicitorLetterDetails.getCaseNumber());
    }

    private CaseDocument generateDocument(String authToken,
                                          IntervenerRemovedSolicitorLetterDetails intervenerRemovedSolicitorLetterDetails,
                                          String template,
                                          String filename) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            convertLetterDetailsToMap(intervenerRemovedSolicitorLetterDetails),
            template,
            filename,
            intervenerRemovedSolicitorLetterDetails.getCaseNumber());
    }

    private IntervenerAddedLetterDetails generateAddedLetterDetails(FinremCaseDetails caseDetails,
                                                                    DocumentHelper.PaperNotificationRecipient recipient) {

        return IntervenerAddedLetterDetails.builder()
            .courtDetails(courtDetailsMapper.getCourtDetails(caseDetails.getData()
                .getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()))
            .addressee(caseDetails.getData().getCurrentAddressee())
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .reference(getSolicitorReference(caseDetails, recipient))
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .caseNumber(caseDetails.getId().toString())
            .intervenerFullName(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerName())
            .build();
    }

    private IntervenerRemovedLetterDetails generateRemovedLetterDetails(FinremCaseDetails caseDetails,
                                                                        DocumentHelper.PaperNotificationRecipient recipient) {

        return IntervenerRemovedLetterDetails.builder()
            .courtDetails(courtDetailsMapper.getCourtDetails(caseDetails.getData()
                .getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()))
            .addressee(caseDetails.getData().getCurrentAddressee())
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .reference(getSolicitorReference(caseDetails, recipient))
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .caseNumber(caseDetails.getId().toString())
            .intervenerFullName(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerName())
            .build();
    }

    private IntervenerAddedSolicitorLetterDetails generateSolAddedLetterDetails(FinremCaseDetails caseDetails,
                                                                                DocumentHelper.PaperNotificationRecipient recipient) {

        return IntervenerAddedSolicitorLetterDetails.builder()
            .courtDetails(courtDetailsMapper.getCourtDetails(caseDetails.getData()
                .getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()))
            .addressee(caseDetails.getData().getCurrentAddressee())
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .reference(getSolicitorReference(caseDetails, recipient))
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .caseNumber(caseDetails.getId().toString())
            .intervenerFullName(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerName())
            .intervenerSolicitorFirm(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails()
                .getIntervenerOrganisation().getOrganisation().getOrganisationName())
            .build();
    }

    private IntervenerRemovedSolicitorLetterDetails generateSolRemovedLetterDetails(FinremCaseDetails caseDetails,
                                                                                    DocumentHelper.PaperNotificationRecipient recipient) {

        return IntervenerRemovedSolicitorLetterDetails.builder()
            .courtDetails(courtDetailsMapper.getCourtDetails(caseDetails.getData()
                .getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()))
            .addressee(caseDetails.getData().getCurrentAddressee())
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .reference(getSolicitorReference(caseDetails, recipient))
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .caseNumber(caseDetails.getId().toString())
            .intervenerFullName(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerName())
            .intervenerSolicitorFirm(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails()
                .getIntervenerOrganisation().getOrganisation().getOrganisationName())
            .build();
    }

    private String getSolicitorReference(FinremCaseDetails caseDetails,
                                         DocumentHelper.PaperNotificationRecipient recipient) {
        ContactDetailsWrapper contactDetailsWrapper = caseDetails.getData().getContactDetailsWrapper();
        if (recipient == APPLICANT
            && YesOrNo.YES.equals(contactDetailsWrapper.getApplicantRepresented())) {
            return contactDetailsWrapper.getSolicitorReference();
        } else if (caseDetails.getData().isContestedApplication()
            && recipient == RESPONDENT
            && YesOrNo.YES.equals(((ContestedContactDetailsWrapper) contactDetailsWrapper)
            .getContestedRespondentRepresented())) {
            return contactDetailsWrapper.getRespondentSolicitorReference();
        }
        return null;
    }

    private Map<String, Object> convertLetterDetailsToMap(IntervenerAddedLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private Map<String, Object> convertLetterDetailsToMap(IntervenerRemovedLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private Map<String, Object> convertLetterDetailsToMap(IntervenerAddedSolicitorLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private Map<String, Object> convertLetterDetailsToMap(IntervenerRemovedSolicitorLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
