package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildInterimHearingFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtIH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService.HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterimHearingService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final InterimHearingHelper interimHearingHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    public List<InterimHearingCollection> getLegacyInterimHearingAsInterimHearingCollection(FinremCaseData caseData) {
        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        InterimRegionWrapper interimRegionWrapper = caseData.getRegionWrapper().getInterimRegionWrapper();
        InterimCourtListWrapper courtListWrapper = interimRegionWrapper.getCourtListWrapper();
        return List.of(
            InterimHearingCollection.builder().value(
                    InterimHearingItem.builder()
                        .interimHearingType(interimWrapper.getInterimHearingType())
                        .interimHearingDate(interimWrapper.getInterimHearingDate())
                        .interimHearingTime(interimWrapper.getInterimHearingTime())
                        .interimHearingTimeEstimate(interimWrapper.getInterimTimeEstimate())
                        .interimAdditionalInformationAboutHearing(
                            interimWrapper.getInterimAdditionalInformationAboutHearing())
                        .interimPromptForAnyDocument(interimWrapper.getInterimPromptForAnyDocument())
                        .interimRegionList(interimRegionWrapper.getInterimRegionList())
                        .interimMidlandsFrcList(interimRegionWrapper.getInterimMidlandsFrcList())
                        .interimLondonFrcList(interimRegionWrapper.getInterimLondonFrcList())
                        .interimNorthWestFrcList(interimRegionWrapper.getInterimNorthWestFrcList())
                        .interimNorthEastFrcList(interimRegionWrapper.getInterimNorthEastFrcList())
                        .interimSouthEastFrcList(interimRegionWrapper.getInterimSouthEastFrcList())
                        .interimSouthWestFrcList(interimRegionWrapper.getInterimSouthWestFrcList())
                        .interimWalesFrcList(interimRegionWrapper.getInterimWalesFrcList())
                        .interimHighCourtFrcList(interimRegionWrapper.getInterimHighCourtFrcList())
                        .interimNottinghamCourtList(courtListWrapper.getInterimNottinghamCourtList())
                        .interimCfcCourtList(courtListWrapper.getInterimCfcCourtList())
                        .interimBirminghamCourtList(courtListWrapper.getInterimBirminghamCourtList())
                        .interimLiverpoolCourtList(courtListWrapper.getInterimLiverpoolCourtList())
                        .interimManchesterCourtList(courtListWrapper.getInterimManchesterCourtList())
                        .interimLancashireCourtList(courtListWrapper.getInterimLancashireCourtList())
                        .interimClevelandCourtList(courtListWrapper.getInterimClevelandCourtList())
                        .interimNwYorkshireCourtList(courtListWrapper.getInterimNwYorkshireCourtList())
                        .interimHumberCourtList(courtListWrapper.getInterimHumberCourtList())
                        .interimKentSurreyCourtList(courtListWrapper.getInterimKentSurreyCourtList())
                        .interimBedfordshireCourtList(courtListWrapper.getInterimBedfordshireCourtList())
                        .interimThamesValleyCourtList(courtListWrapper.getInterimThamesValleyCourtList())
                        .interimDevonCourtList(courtListWrapper.getInterimDevonCourtList())
                        .interimDorsetCourtList(courtListWrapper.getInterimDorsetCourtList())
                        .interimBristolCourtList(courtListWrapper.getInterimBristolCourtList())
                        .interimNewportCourtList(courtListWrapper.getInterimNewportCourtList())
                        .interimSwanseaCourtList(courtListWrapper.getInterimSwanseaCourtList())
                        .interimNorthWalesCourtList(courtListWrapper.getInterimNorthWalesCourtList())
                        .interimHighCourtList(courtListWrapper.getInterimHighCourtList())
                        .interimUploadAdditionalDocument(
                            interimWrapper.getInterimUploadAdditionalDocument())
                        .build())
                .build());
    }

    public void addHearingNoticesToCase(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("In submitInterimHearing for Case ID: {}", caseDetails.getId());
        List<InterimHearingCollection> newInterimHearingList =
            caseDetails.getData().getInterimWrapper().getInterimHearingsScreenField();

        if (!newInterimHearingList.isEmpty()) {
            CaseDocumentsHolder caseDocumentsHolder =
                prepareDocumentsForPrint(caseDetails, newInterimHearingList, authorisationToken);
            sendToBulkPrint(caseDetails, authorisationToken, caseDocumentsHolder);
        }

    }

    public List<String> getValidationErrors(FinremCaseData caseData) {
        List<InterimHearingCollection> interimHearingList =
            caseData.getInterimWrapper().getInterimHearingsScreenField();

        List<String> errors = new ArrayList<>();
        if (!interimHearingList.isEmpty()) {
            selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(caseData);
            errors = selectablePartiesCorrespondenceService
                .validateApplicantAndRespondentCorrespondenceAreSelected(
                    caseData, HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE);
        }

        return errors;
    }


    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private void sendToBulkPrint(FinremCaseDetails finremCaseDetails, String authorisationToken,
                                 CaseDocumentsHolder caseDocumentsHolder) {

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending interim hearing documents to applicant - bulk print for Case ID: {}", finremCaseDetails.getId());
            bulkPrintService.printApplicantDocuments(finremCaseDetails, authorisationToken, caseDocumentsHolder.getBulkPrintDocuments());
        }
        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending interim hearing documents to respondent - bulk print for Case ID: {}", finremCaseDetails.getId());
            bulkPrintService.printRespondentDocuments(finremCaseDetails, authorisationToken, caseDocumentsHolder.getBulkPrintDocuments());
        }
        sendToBulkPrintForInterveners(authorisationToken, finremCaseDetails, caseDocumentsHolder);
    }

    @SuppressWarnings("java:S1874")
    private void sendToBulkPrintForInterveners(String authorisationToken, FinremCaseDetails finremCaseDetails,
                                               CaseDocumentsHolder caseDocumentsHolder) {

        final List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (intervenerWrapper.getIntervenerCorrespondenceEnabled() != null
                && Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled())) {
                addCaseDocumentsToIntervenerHearingNotices(
                    intervenerWrapper, caseDocumentsHolder, finremCaseDetails.getData());
                if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, finremCaseDetails)) {
                    log.info("Sending letter correspondence to {} for Case ID: {}",
                        intervenerWrapper.getIntervenerType().getTypeValue(),
                        finremCaseDetails.getId());
                    bulkPrintService.printIntervenerDocuments(intervenerWrapper, finremCaseDetails, authorisationToken,
                        caseDocumentsHolder.getBulkPrintDocuments());
                }
            }
        });
    }

    private void addCaseDocumentsToIntervenerHearingNotices(IntervenerWrapper intervenerWrapper,
                                                            CaseDocumentsHolder caseDocumentsHolder,
                                                            FinremCaseData finremCaseData) {
        List<IntervenerHearingNoticeCollection> intervenerHearingNoticesCollection =
            intervenerWrapper.getIntervenerHearingNoticesCollection(finremCaseData);
        caseDocumentsHolder.getCaseDocuments()
            .forEach(cd -> intervenerHearingNoticesCollection.add(getHearingNoticesDocumentCollection(cd)));
    }

    private IntervenerHearingNoticeCollection getHearingNoticesDocumentCollection(CaseDocument hearingNotice) {
        return IntervenerHearingNoticeCollection.builder()
            .value(IntervenerHearingNotice.builder().caseDocument(hearingNotice)
                .noticeReceivedAt(LocalDateTime.now()).build()).build();
    }

    @SuppressWarnings("java:S6204")
    private CaseDocumentsHolder prepareDocumentsForPrint(FinremCaseDetails caseDetails,
                                                         List<InterimHearingCollection> newInterimHearingList,
                                                         String authorisationToken) {


        String caseId = caseDetails.getId().toString();
        log.info("preparing for bulk print document for Case ID: {}", caseId);
        List<CaseDocument> newInterimHearingNotices = prepareInterimHearingRequiredNoticeDocument(caseDetails,
            newInterimHearingList, authorisationToken);

        CaseDocumentsHolder caseDocumentsHolder = CaseDocumentsHolder.builder()
            .caseDocuments(new ArrayList<>())
            .bulkPrintDocuments(new ArrayList<>())
            .build();
        caseDocumentsHolder.getCaseDocuments().addAll(newInterimHearingNotices);

        List<BulkPrintDocument> documents = newInterimHearingNotices.stream()
            .map(documentHelper::mapToBulkPrintDocument).collect(Collectors.toList());
        caseDocumentsHolder.getBulkPrintDocuments().addAll(documents);

        addUploadedDocumentsToBulkPrintList(caseId, newInterimHearingList, caseDocumentsHolder, authorisationToken);

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList = new ArrayList<>();
        bulkPrintDocumentsList.addAll(
            caseDocumentsHolder.getCaseDocuments().stream().map(this::loadBulkPrintDocument).toList());
        if (caseDetails.getData().getInterimWrapper().getInterimHearingDocuments() == null) {
            caseDetails.getData().getInterimWrapper().setInterimHearingDocuments(new ArrayList<>());
        }
        caseDetails.getData().getInterimWrapper().getInterimHearingDocuments().addAll(bulkPrintDocumentsList);
        return caseDocumentsHolder;
    }

    private void addUploadedDocumentsToBulkPrintList(String caseId,
                                                     List<InterimHearingCollection> interimHearingList,
                                                     CaseDocumentsHolder caseDocumentsHolder,
                                                     String authorisationToken) {
        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(interimHearingList);
        interimCaseData.forEach(interimData -> addToBulkPrintList(caseId, interimData, caseDocumentsHolder, authorisationToken));
    }

    private void addToBulkPrintList(String caseId, Map<String, Object> interimData,
                                    CaseDocumentsHolder caseDocumentsHolder, String authorisationToken) {
        String isDocUploaded = nullToEmpty(interimData.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT));
        if ("Yes".equalsIgnoreCase(isDocUploaded)) {
            log.warn("Additional uploaded interim document found for printing for Case ID: {}", caseId);
            CaseDocument caseDocument =
                documentHelper.convertToCaseDocument(interimData.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
            CaseDocument additionalUploadedDocuments =
                genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken, caseId);
            caseDocumentsHolder.getBulkPrintDocuments().add(
                documentHelper.mapToBulkPrintDocument(additionalUploadedDocuments));
        }
    }

    private InterimHearingBulkPrintDocumentsData loadBulkPrintDocument(CaseDocument generatedDocument) {

        return InterimHearingBulkPrintDocumentsData.builder().id(UUID.randomUUID().toString())
            .value(InterimHearingBulkPrintDocument.builder()
                .caseDocument(CaseDocument.builder()
                    .documentUrl(generatedDocument.getDocumentUrl())
                    .documentFilename(generatedDocument.getDocumentFilename())
                    .documentBinaryUrl(generatedDocument.getDocumentBinaryUrl())
                    .build()).build())
            .build();
    }

    private List<CaseDocument> prepareInterimHearingRequiredNoticeDocument(FinremCaseDetails caseDetails,
                                                                           List<InterimHearingCollection> newInterimHearingList,
                                                                           String authorisationToken) {

        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(newInterimHearingList);

        return interimCaseData.stream()
            .map(interimHearingCaseData -> generateInterimHearingNotice(interimHearingCaseData, caseDetails, authorisationToken))
            .toList();
    }

    @SuppressWarnings("java:S6204")
    public List<Map<String, Object>> convertInterimHearingCollectionDataToMap(
        List<InterimHearingCollection> interimHearingList) {

        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingCollection::getValue).toList();
        return interimHearingItems.stream()
            .map(obj -> objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();
    }

    private CaseDocument generateInterimHearingNotice(Map<String, Object> interimHearingCaseData,
                                                      FinremCaseDetails caseDetails,
                                                      String authorisationToken) {

        CaseDetails caseDetailsCopy = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put("ccdCaseNumber", caseDetailsCopy.getId());
        caseData.put("courtDetails", buildInterimHearingFrcCourtDetails(interimHearingCaseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        addInterimHearingVenueDetails(caseDetailsCopy, interimHearingCaseData);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));
        caseData.put("interimHearingType", interimHearingCaseData.get("interimHearingType"));
        caseData.put("interimHearingDate", interimHearingCaseData.get("interimHearingDate"));
        caseData.put("interimHearingTime", interimHearingCaseData.get("interimHearingTime"));
        caseData.put("interimTimeEstimate", interimHearingCaseData.get("interimHearingTimeEstimate"));
        caseData.put("interimAdditionalInformationAboutHearing",
            interimHearingCaseData.get("interimAdditionalInformationAboutHearing"));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(caseDetailsCopy),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());

    }

    private void addInterimHearingVenueDetails(CaseDetails caseDetailsCopy, Map<String, Object> interimHearingCaseData) {
        Map<String, Object> caseData = caseDetailsCopy.getData();
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), new TypeReference<>() {
            });
            String selectedCourtIH = getSelectedCourtIH(interimHearingCaseData);
            String courtDetailsObj = (String) interimHearingCaseData.get(selectedCourtIH);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(courtDetailsObj);
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public void clearLegacyInterimData(FinremCaseData caseData) {

        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        interimWrapper.setInterimHearingType(null);
        interimWrapper.setInterimHearingDate(null);
        interimWrapper.setInterimHearingTime(null);
        interimWrapper.setInterimTimeEstimate(null);
        interimWrapper.setInterimAdditionalInformationAboutHearing(null);
        interimWrapper.setInterimPromptForAnyDocument(null);
        InterimRegionWrapper interimRegionWrapper = caseData.getRegionWrapper().getInterimRegionWrapper();
        interimRegionWrapper.setInterimRegionList(null);
        interimRegionWrapper.setInterimLondonFrcList(null);
        interimRegionWrapper.setInterimHighCourtFrcList(null);
        interimRegionWrapper.setInterimNorthEastFrcList(null);
        interimRegionWrapper.setInterimMidlandsFrcList(null);
        interimRegionWrapper.setInterimNorthWestFrcList(null);
        interimRegionWrapper.setInterimSouthEastFrcList(null);
        interimRegionWrapper.setInterimSouthWestFrcList(null);
        interimRegionWrapper.setInterimWalesFrcList(null);
        InterimCourtListWrapper courtListWrapper = interimRegionWrapper.getCourtListWrapper();
        courtListWrapper.setInterimNottinghamCourtList(null);
        courtListWrapper.setInterimCfcCourtList(null);
        courtListWrapper.setInterimBirminghamCourtList(null);
        courtListWrapper.setInterimLiverpoolCourtList(null);
        courtListWrapper.setInterimManchesterCourtList(null);
        courtListWrapper.setInterimLancashireCourtList(null);
        courtListWrapper.setInterimClevelandCourtList(null);
        courtListWrapper.setInterimNwYorkshireCourtList(null);
        courtListWrapper.setInterimHumberCourtList(null);
        courtListWrapper.setInterimKentSurreyCourtList(null);
        courtListWrapper.setInterimBedfordshireCourtList(null);
        courtListWrapper.setInterimThamesValleyCourtList(null);
        courtListWrapper.setInterimDevonCourtList(null);
        courtListWrapper.setInterimDorsetCourtList(null);
        courtListWrapper.setInterimBristolCourtList(null);
        courtListWrapper.setInterimNewportCourtList(null);
        courtListWrapper.setInterimSwanseaCourtList(null);
        courtListWrapper.setInterimNorthWalesCourtList(null);

        interimWrapper.setInterimHearingCollectionItemIds(null);
    }

    public void sendNotification(FinremCaseDetails caseDetails) {
        log.info("Sending email notification for Case ID: {}", caseDetails.getId());
        List<InterimHearingCollection> newInterimHearingsToSendNotifications =
            caseDetails.getData().getInterimWrapper().getInterimHearingsScreenField();
        List<Map<String, Object>> newInterimHearingsMap =
            convertInterimHearingCollectionDataToMap(newInterimHearingsToSendNotifications);
        newInterimHearingsMap.forEach(interimHearingData -> notify(caseDetails, interimHearingData));

    }

    private void notify(FinremCaseDetails caseDetails, Map<String, Object> interimHearingData) {

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(caseDetails.getData());

        if (notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
            && caseDetails.getData().isApplicantCorrespondenceEnabled()) {
            log.info("Sending email notification to Applicant Solicitor about interim hearing for Case ID: {}", caseDetails.getId());
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
            && caseDetails.getData().isRespondentCorrespondenceEnabled()) {
            log.info("Sending email notification to Respondent Solicitor about interim hearing for Case ID: {}", caseDetails.getId());
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.isContestedApplication(caseDetails)) {

            final List<IntervenerWrapper> interveners = caseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper -> {
                if (notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails)
                    && (intervenerWrapper.getIntervenerCorrespondenceEnabled() != null
                    && Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled()))) {
                    log.info("Sending email notification to {} Solicitor about interim hearing for Case ID: {}",
                        intervenerWrapper.getIntervenerType().getTypeValue(),
                        caseDetails.getId());
                    notificationService.sendInterimHearingNotificationEmailToIntervenerSolicitor(caseDetails, interimHearingData,
                        notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
                }
            });
        }
    }


}
