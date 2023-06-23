package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_SUMMARIES_COLLECTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntervenerShareDocumentsService implements SharedService {

    private final ApplicantShareDocumentsService appService;
    private final RespondentShareDocumentsService respService;
    public static final String SUMMARIES = "SUMMARIES";
    public static final String CHRONOLOGIES = "CHRONOLOGIES";
    public static final String CORRESPONDENCE = "CORRESPONDENCE";
    public static final String EXPERT_EVIDENCE = "EXPERT_EVIDENCE";
    public static final String FORM_E_EXHIBITS = "FORM_E_EXHIBITS";
    public static final String FORM_H = "FORM_H";
    public static final String HEARING_BUNDLES = "HEARING_BUNDLES";
    public static final String OTHER = "OTHER";
    public static final String QUESTIONNAIRES_ANSWERS = "QUESTIONNAIRES_ANSWERS";
    public static final String STATEMENTS_EXHIBITS = "STATEMENTS_EXHIBITS";

    public DynamicMultiSelectList intervenerSourceDocumentList(FinremCaseDetails caseDetails, String role) {

        log.info("Setting intervener source document list for case {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        List<UploadCaseDocumentCollection> otherCollection
            = caseData.getUploadCaseDocumentWrapper().getIntv1Other();

        if (ObjectUtils.isNotEmpty(otherCollection)) {
            otherCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, OTHER);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> chronologiesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
        if (ObjectUtils.isNotEmpty(chronologiesCollection)) {
            chronologiesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, CHRONOLOGIES);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> statementsExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
        if (ObjectUtils.isNotEmpty(statementsExhibitsCollection)) {
            statementsExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, STATEMENTS_EXHIBITS);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> hearingBundlesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
        if (ObjectUtils.isNotEmpty(hearingBundlesCollection)) {
            hearingBundlesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, HEARING_BUNDLES);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> formEExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
        if (ObjectUtils.isNotEmpty(formEExhibitsCollection)) {
            formEExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, FORM_E_EXHIBITS);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appQaCollection
            = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
        if (ObjectUtils.isNotEmpty(appQaCollection)) {
            appQaCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, QUESTIONNAIRES_ANSWERS);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appCaseSummariesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
        if (ObjectUtils.isNotEmpty(appCaseSummariesCollection)) {
            appCaseSummariesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, SUMMARIES);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appFormsHCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
        if (ObjectUtils.isNotEmpty(appFormsHCollection)) {
            appFormsHCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, FORM_H);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> appExpertEvidenceCollection
            = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
        if (ObjectUtils.isNotEmpty(appExpertEvidenceCollection)) {
            appExpertEvidenceCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, EXPERT_EVIDENCE);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> appCorrespondenceDocsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
        if (ObjectUtils.isNotEmpty(appCorrespondenceDocsCollection)) {
            appCorrespondenceDocsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                String collection = getIntervenerOtherCollection(role, CORRESPONDENCE);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + collection,
                    collection + " -> " + filename));
            });
        }

        return getSelectedDocumentList(dynamicListElements, caseData.getSourceDocumentList());
    }


    private String getIntervenerOtherCollection(String role, String collectionType) {
        if (role.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            return getIntervenerOneCollectionType(collectionType, role);
        } else if (role.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            return getIntervenerTwoCollectionType(collectionType, role);
        } else if (role.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            return getIntervenerThreeCollectionType(collectionType, role);
        } else if (role.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            return getIntervenerFourCollectionType(collectionType, role);
        }
        return null;
    }

    private String getIntervenerOneCollectionType(String collectionType, String role) {
        if (role.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            switch (collectionType) {
                case OTHER -> {
                    return INTERVENER_ONE_OTHER_COLLECTION.getCcdKey();
                }
                case CHRONOLOGIES -> {
                    return INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey();
                }
                case STATEMENTS_EXHIBITS -> {
                    return INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey();
                }
                case HEARING_BUNDLES -> {
                    return INTERVENER_ONE_HEARING_BUNDLES_COLLECTION.getCcdKey();
                }
                case FORM_E_EXHIBITS -> {
                    return INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
                }
                case QUESTIONNAIRES_ANSWERS -> {
                    return INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
                }
                case SUMMARIES -> {
                    return INTERVENER_ONE_SUMMARIES_COLLECTION.getCcdKey();
                }
                case FORM_H -> {
                    return INTERVENER_ONE_FORM_H_COLLECTION.getCcdKey();
                }
                case EXPERT_EVIDENCE -> {
                    return INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
                }
                case CORRESPONDENCE -> {
                    return INTERVENER_ONE_CORRESPONDENCE_COLLECTION.getCcdKey();
                }
                default -> log.info("Invalid choice made for intervener one");
            }

        }
        return null;
    }

    private String getIntervenerTwoCollectionType(String collectionType, String role) {
        if (role.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            switch (collectionType) {
                case OTHER -> {
                    return INTERVENER_TWO_OTHER_COLLECTION.getCcdKey();
                }
                case CHRONOLOGIES -> {
                    return INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey();
                }
                case STATEMENTS_EXHIBITS -> {
                    return INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey();
                }
                case HEARING_BUNDLES -> {
                    return INTERVENER_TWO_HEARING_BUNDLES_COLLECTION.getCcdKey();
                }
                case FORM_E_EXHIBITS -> {
                    return INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
                }
                case QUESTIONNAIRES_ANSWERS -> {
                    return INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
                }
                case SUMMARIES -> {
                    return INTERVENER_TWO_SUMMARIES_COLLECTION.getCcdKey();
                }
                case FORM_H -> {
                    return INTERVENER_TWO_FORM_H_COLLECTION.getCcdKey();
                }
                case EXPERT_EVIDENCE -> {
                    return INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
                }
                case CORRESPONDENCE -> {
                    return INTERVENER_TWO_CORRESPONDENCE_COLLECTION.getCcdKey();
                }
                default -> log.info("Invalid choice made for intervener two");
            }

        }
        return null;
    }

    private String getIntervenerThreeCollectionType(String collectionType, String role) {
        if (role.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            switch (collectionType) {
                case OTHER -> {
                    return INTERVENER_THREE_OTHER_COLLECTION.getCcdKey();
                }
                case CHRONOLOGIES -> {
                    return INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey();
                }
                case STATEMENTS_EXHIBITS -> {
                    return INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey();
                }
                case HEARING_BUNDLES -> {
                    return INTERVENER_THREE_HEARING_BUNDLES_COLLECTION.getCcdKey();
                }
                case FORM_E_EXHIBITS -> {
                    return INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
                }
                case QUESTIONNAIRES_ANSWERS -> {
                    return INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
                }
                case SUMMARIES -> {
                    return INTERVENER_THREE_SUMMARIES_COLLECTION.getCcdKey();
                }
                case FORM_H -> {
                    return INTERVENER_THREE_FORM_H_COLLECTION.getCcdKey();
                }
                case EXPERT_EVIDENCE -> {
                    return INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
                }
                case CORRESPONDENCE -> {
                    return INTERVENER_THREE_CORRESPONDENCE_COLLECTION.getCcdKey();
                }
                default -> log.info("Invalid choice made for intervener three");
            }
        }
        return null;
    }

    private String getIntervenerFourCollectionType(String collectionType, String role) {
        if (role.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || role.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            switch (collectionType) {
                case OTHER -> {
                    return INTERVENER_FOUR_OTHER_COLLECTION.getCcdKey();
                }
                case CHRONOLOGIES -> {
                    return INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey();
                }
                case STATEMENTS_EXHIBITS -> {
                    return INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey();
                }
                case HEARING_BUNDLES -> {
                    return INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION.getCcdKey();
                }
                case FORM_E_EXHIBITS -> {
                    return INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
                }
                case QUESTIONNAIRES_ANSWERS -> {
                    return INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey();
                }
                case SUMMARIES -> {
                    return INTERVENER_FOUR_SUMMARIES_COLLECTION.getCcdKey();
                }
                case FORM_H -> {
                    return INTERVENER_FOUR_FORM_H_COLLECTION.getCcdKey();
                }
                case EXPERT_EVIDENCE -> {
                    return INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
                }
                case CORRESPONDENCE -> {
                    return INTERVENER_FOUR_CORRESPONDENCE_COLLECTION.getCcdKey();
                }
                default -> log.info("Invalid choice made for intervener four");
            }

        }
        return null;
    }

    public DynamicMultiSelectList getOtherSolicitorRoleList(FinremCaseDetails caseDetails) {
        log.info("fetching all parties solicitor case role for caseId {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<String> roleList = new ArrayList<>();
        //applicant
        if (ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID())) {
            roleList.add(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
        //respondent
        if (ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID())) {
            roleList.add(caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        return getRoleList(intervenerCaseRoleList(caseData, roleList),
            caseDetails.getData().getSolicitorRoleList());
    }

    public void shareSelectedDocumentWithOtherSelectedSolicitors(FinremCaseData caseData) {
        DynamicMultiSelectList sourceDocumentList = caseData.getSourceDocumentList();
        DynamicMultiSelectList solicitorRoleList = caseData.getSolicitorRoleList();

        if (ObjectUtils.isNotEmpty(sourceDocumentList) && ObjectUtils.isNotEmpty(solicitorRoleList)) {
            List<DynamicMultiSelectListElement> roleList = solicitorRoleList.getValue();
            roleList.forEach(role -> {
                List<DynamicMultiSelectListElement> documentList = sourceDocumentList.getValue();
                copySelectedFilesToTargetCollection(caseData, role.getCode(), documentList);
                copyIntervenersDocuments(caseData, role.getCode(), documentList);
            });
        }
    }


    private void copySelectedFilesToTargetCollection(FinremCaseData caseData, String role, List<DynamicMultiSelectListElement> documentList) {
        if (role.equals(CaseRole.APP_SOLICITOR.getValue()) || role.equals(CaseRole.APP_BARRISTER.getValue())) {
            appService.copySelectedFilesToTargetCollection(caseData, role, documentList);
        }
        if (role.equals(CaseRole.RESP_SOLICITOR.getValue()) || role.equals(CaseRole.RESP_BARRISTER.getValue())) {
            respService.copySelectedFilesToTargetCollection(caseData, role, documentList);
        }
    }
}
