package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION;

@Component
public class ExpertEvidenceDocumentSharer extends DocumentSharer {

    @Override
    protected void setRespondentSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setRespExpertEvidenceCollShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollShared();
    }


    @Override
    protected void setApplicantSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setAppExpertEvidenceCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv1ExpertEvidenceShared();
    }

    @Override
    protected void setIntervenerOneSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv1ExpertEvidenceShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv2ExpertEvidenceShared();
    }

    @Override
    protected void setIntervenerTwoSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv2ExpertEvidenceShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv3ExpertEvidenceShared();
    }

    @Override
    protected void setIntervenerThreeSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv3ExpertEvidenceShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv4ExpertEvidenceShared();
    }

    @Override
    protected void setIntervenerFourSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv4ExpertEvidenceShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollectionShared();
    }


    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv4ExpertEvidence();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv3ExpertEvidence();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv2ExpertEvidence();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv1ExpertEvidence();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getRespExpertEvidenceCollection();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getAppExpertEvidenceCollection();
    }

    @Override
    protected String getIntervenerFourCollectionCcdKey() {
        return INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerThreeCollectionCcdKey() {
        return INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerTwoCollectionCcdKey() {
        return INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerOneCollectionCcdKey() {
        return INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getRespondentCollectionCcdKey() {
        return RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getApplicantCollectionCcdKey() {
        return APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey();
    }
}
