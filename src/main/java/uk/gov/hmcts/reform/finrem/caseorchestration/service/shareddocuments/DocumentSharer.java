package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public abstract class DocumentSharer {

    private final FeatureToggleService featureToggleService;

    protected DocumentSharer(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    public void shareDocumentsToSharedPartyCollection(FinremCaseData caseData,
                                                      String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> documentCollectionToShare = getDocumentCollection(caseData, collName);

        log.info("Sharing documents for case {} and case role {} and collection name {} and collection id {}",
            caseData.getCcdCaseId(), caseRole, collName, collId);
        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            log.info("Inside RESP_SOLICITOR OR RESP_BARRISTER");
            setRespondentSharedCollection(caseData, getAndAddToExistingSharedCollection(collId, documentCollectionToShare,
                getRespondentSharedCollection(caseData)));
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            log.info("Inside APP_SOLICITOR OR APP_BARRISTER");
            setApplicantSharedCollection(caseData, getAndAddToExistingSharedCollection(collId, documentCollectionToShare,
                getApplicantSharedCollection(caseData)));
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode())) {
            log.info("Inside INTVR_SOLICITOR_1 OR INTVR_SOLICITOR_1");
            setIntervenerOneSharedCollection(caseData, getAndAddToExistingSharedCollection(collId, documentCollectionToShare,
                getIntervenerOneSharedCollection(caseData)));
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntervenerTwoSharedCollection(caseData, getAndAddToExistingSharedCollection(collId, documentCollectionToShare,
                getIntervenerTwoSharedCollection(caseData)));
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntervenerThreeSharedCollection(caseData, getAndAddToExistingSharedCollection(collId, documentCollectionToShare,
                getIntervenerThreeSharedCollection(caseData)));
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntervenerFourSharedCollection(caseData, getAndAddToExistingSharedCollection(collId, documentCollectionToShare,
                getIntervenerFourSharedCollection(caseData)));
        }
    }


    public List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData, String collectionName) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collectionName.equalsIgnoreCase(getApplicantCollectionCcdKey())) {
            return getApplicantCollection(documentWrapper);
        } else if (collectionName.equalsIgnoreCase(getRespondentCollectionCcdKey())) {
            return getRespondentCollection(documentWrapper);
        } else if (collectionName.equalsIgnoreCase(getIntervenerOneCollectionCcdKey())) {
            return getIntervenerOneCollection(documentWrapper);
        } else if (collectionName.equalsIgnoreCase(getIntervenerTwoCollectionCcdKey())) {
            return getIntervenerTwoCollection(documentWrapper);
        } else if (collectionName.equalsIgnoreCase(getIntervenerThreeCollectionCcdKey())) {
            return getIntervenerThreeCollection(documentWrapper);
        } else if (collectionName.equalsIgnoreCase(getIntervenerFourCollectionCcdKey())) {
            return getIntervenerFourCollection(documentWrapper);
        }
        return new ArrayList<>();
    }


    private List<UploadCaseDocumentCollection> getAndAddToExistingSharedCollection(String collId,
                                                                                   List<UploadCaseDocumentCollection> documentCollectionToShare,
                                                                                   List<UploadCaseDocumentCollection> sharedCollection) {
        log.info("Document to share ------{}", documentCollectionToShare);
        List<UploadCaseDocumentCollection> list =
            Optional.ofNullable(sharedCollection)
                .orElse(new ArrayList<>());
        if (documentCollectionToShare != null) {
            documentCollectionToShare.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    list.add(copyUploadCaseDocumentCollection(sd));
                }
            });
        }
        log.info("Document to share final list ------{}", list);
        return list;
    }

    private UploadCaseDocumentCollection copyUploadCaseDocumentCollection(UploadCaseDocumentCollection sd) {
        log.info("copyUploadCaseDocumentCollection {}", sd);
        UploadCaseDocument uploadCaseDocument = sd.getUploadCaseDocument();
        UploadCaseDocument uploadCaseDocumentToCopy = new UploadCaseDocument(uploadCaseDocument);
        if (featureToggleService.isCaseFileViewEnabled()) {
            uploadCaseDocumentToCopy.getCaseDocuments().setCategoryId(DocumentCategory.SHARED.getDocumentCategoryId());
        }
        return UploadCaseDocumentCollection.builder()
            .id(UUID.randomUUID().toString())
            .uploadCaseDocument(uploadCaseDocumentToCopy).build();
    }

    protected abstract List<UploadCaseDocumentCollection> getIntervenerFourCollection(UploadCaseDocumentWrapper documentWrapper);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerThreeCollection(UploadCaseDocumentWrapper documentWrapper);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerTwoCollection(UploadCaseDocumentWrapper documentWrapper);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerOneCollection(UploadCaseDocumentWrapper documentWrapper);

    protected abstract List<UploadCaseDocumentCollection> getRespondentCollection(UploadCaseDocumentWrapper documentWrapper);

    protected abstract List<UploadCaseDocumentCollection> getApplicantCollection(UploadCaseDocumentWrapper documentWrapper);

    protected abstract String getIntervenerFourCollectionCcdKey();

    protected abstract String getIntervenerThreeCollectionCcdKey();

    protected abstract String getIntervenerTwoCollectionCcdKey();

    protected abstract String getIntervenerOneCollectionCcdKey();

    protected abstract String getRespondentCollectionCcdKey();

    protected abstract String getApplicantCollectionCcdKey();

    protected abstract void setRespondentSharedCollection(FinremCaseData caseData,
                                                          List<UploadCaseDocumentCollection> andAddToExistingSharedCollection);

    protected abstract List<UploadCaseDocumentCollection> getRespondentSharedCollection(FinremCaseData caseData);

    protected abstract void setApplicantSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerOneSharedCollection(FinremCaseData caseData);

    protected abstract void setIntervenerOneSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerTwoSharedCollection(FinremCaseData caseData);

    protected abstract void setIntervenerTwoSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerThreeSharedCollection(FinremCaseData caseData);

    protected abstract void setIntervenerThreeSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list);

    protected abstract List<UploadCaseDocumentCollection> getIntervenerFourSharedCollection(FinremCaseData caseData);

    protected abstract void setIntervenerFourSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list);

    protected abstract List<UploadCaseDocumentCollection> getApplicantSharedCollection(FinremCaseData caseData);

}
