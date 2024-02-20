package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES;

@Slf4j
@Service
public class ManageScannedDocsContestedAboutToSubmitHandler extends FinremCallbackHandler {

    public static final String CHOOSE_A_DIFFERENT_PARTY = " not present on the case, do you want to continue?";
    public static final String INTERVENER_1 = "Intervener 1 ";
    public static final String INTERVENER_2 = "Intervener 2 ";
    public static final String INTERVENER_3 = "Intervener 3 ";
    public static final String INTERVENER_4 = "Intervener 4 ";

    private static final List<CaseDocumentType> ADMINISTRATIVE_CASE_DOCUMENT_TYPES = List.of(
        CaseDocumentType.ATTENDANCE_SHEETS,
        CaseDocumentType.JUDICIAL_NOTES,
        CaseDocumentType.JUDGMENT,
        CaseDocumentType.WITNESS_SUMMONS,
        CaseDocumentType.TRANSCRIPT
    );
    private final List<DocumentHandler> documentHandlers;

    @Autowired
    public ManageScannedDocsContestedAboutToSubmitHandler(
        FinremCaseDetailsMapper finremCaseDetailsMapper, List<DocumentHandler> documentHandlers) {
        super(finremCaseDetailsMapper);
        this.documentHandlers = documentHandlers;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.MANAGE_SCANNED_DOCS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        log.info("Received request to manage scanned documents for Case ID : {}",
            callbackRequest.getCaseDetails().getId());
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<String> warnings = new ArrayList<>();
        getValidatedResponse(caseData, warnings);

        List<UploadCaseDocumentCollection> manageScannedDocumentCollection =
            caseData.getManageScannedDocumentCollection().stream()
                .filter(msdc -> YES == msdc.getManageScannedDocument().getSelectForUpdate())
                .map(ManageScannedDocumentCollection::toUploadCaseDocumentCollection)
                .collect(Collectors.toList());

        updateFileNames(manageScannedDocumentCollection);
        addDefaultsToAdministrativeDocuments(manageScannedDocumentCollection);

        List<String> processedScannedDocumentIds = manageScannedDocumentCollection.stream()
            .map(UploadCaseDocumentCollection::getId)
            .collect(Collectors.toList());

        documentHandlers.forEach(documentCollectionService ->
            documentCollectionService.replaceManagedDocumentsInCollectionType(callbackRequest,
                manageScannedDocumentCollection, false));

        manageScannedDocumentCollection.forEach(sd -> processedScannedDocumentIds.remove(sd.getId()));
        removeProcessedScannedDocumentsFromCase(caseData, processedScannedDocumentIds);
        caseData.setManageScannedDocumentCollection(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).warnings(warnings).build();
    }

    private void updateFileNames(List<UploadCaseDocumentCollection> manageScannedDocumentCollection) {
        manageScannedDocumentCollection.forEach(uploadCaseDocumentCollection -> {
            UploadCaseDocument uploadCaseDocument = uploadCaseDocumentCollection.getUploadCaseDocument();
            CaseDocument caseDocument = uploadCaseDocument.getCaseDocuments();
            uploadCaseDocument.setScannedFileName(caseDocument.getDocumentFilename());
            if (StringUtils.isEmpty(uploadCaseDocument.getFileName())) {
                caseDocument.setDocumentFilename(caseDocument.getDocumentFilename());
            } else if (StringUtils.isEmpty(Files.getFileExtension(uploadCaseDocument.getFileName()))) {
                caseDocument.setDocumentFilename(uploadCaseDocument.getFileName() + "."
                    + Files.getFileExtension(caseDocument.getDocumentFilename()));
            } else {
                caseDocument.setDocumentFilename(uploadCaseDocument.getFileName());
            }
        });
    }

    private void getValidatedResponse(FinremCaseData caseData, List<String> warnings) {
        List<ManageScannedDocumentCollection> manageScannedDocumentCollection =
            caseData.getManageScannedDocumentCollection();

        if (StringUtils.isBlank(caseData.getIntervenerOneWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_ONE, manageScannedDocumentCollection)) {
            warnings.add(INTERVENER_1 + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerTwoWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_TWO, manageScannedDocumentCollection)) {
            warnings.add(INTERVENER_2 + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerThreeWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_THREE, manageScannedDocumentCollection)) {
            warnings.add(INTERVENER_3 + CHOOSE_A_DIFFERENT_PARTY);
        }
        if (StringUtils.isBlank(caseData.getIntervenerFourWrapper().getIntervenerName())
            && isIntervenerPartySelected(CaseDocumentParty.INTERVENER_FOUR, manageScannedDocumentCollection)) {
            warnings.add(INTERVENER_4 + CHOOSE_A_DIFFERENT_PARTY);
        }
    }

    private boolean isIntervenerPartySelected(CaseDocumentParty caseDocumentParty,
                                              List<ManageScannedDocumentCollection> manageScannedDocumentCollection) {
        return manageScannedDocumentCollection.stream().anyMatch(documentCollection -> {
            if (documentCollection.getManageScannedDocument().getUploadCaseDocument().getCaseDocumentParty() != null) {
                return caseDocumentParty.equals(
                    documentCollection.getManageScannedDocument().getUploadCaseDocument().getCaseDocumentParty());
            }
            return false;
        });
    }

    private void addDefaultsToAdministrativeDocuments(List<UploadCaseDocumentCollection> managedCollections) {
        managedCollections.forEach(this::setDefaultsForDocumentTypes);
    }

    private void setDefaultsForDocumentTypes(UploadCaseDocumentCollection document) {
        UploadCaseDocument uploadCaseDocument = document.getUploadCaseDocument();
        if (ADMINISTRATIVE_CASE_DOCUMENT_TYPES.contains(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentParty(CaseDocumentParty.CASE);
            uploadCaseDocument.setCaseDocumentConfidentiality(NO);
            uploadCaseDocument.setCaseDocumentFdr(NO);
        } else if (CaseDocumentType.WITHOUT_PREJUDICE_OFFERS.equals(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentConfidentiality(NO);
            uploadCaseDocument.setCaseDocumentFdr(YES);
            uploadCaseDocument.setCaseDocumentParty(null);
        }

    }

    private void removeProcessedScannedDocumentsFromCase(
        FinremCaseData caseData, List<String> processedScannedDocumentIds) {
        processedScannedDocumentIds.forEach(id -> removeScannedDocumentFromCase(caseData, id));
    }

    private void removeScannedDocumentFromCase(FinremCaseData caseData, String id) {
        Optional<ScannedDocumentCollection> scannedDocument = caseData.getScannedDocuments().stream()
            .filter(sdc -> sdc.getId().equals(id))
            .findAny();

        scannedDocument.ifPresent(sdc -> caseData.getScannedDocuments().remove(sdc));
    }
}
