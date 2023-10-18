package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APPLICANT_CORRESPONDENCE_DOC_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_CORRESPONDENCE_COLLECTION;

@Component
public class CorrespondenceDocumentSharer extends DocumentSharer {

    @Override
    protected void setRespondentSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setRespCorrespondenceDocsCollShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsCollShared();
    }


    @Override
    protected void setApplicantSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setAppCorrespondenceDocsCollShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv1CorrespDocsShared();
    }

    @Override
    protected void setIntervenerOneSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv1CorrespDocsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv2CorrespDocsShared();
    }

    @Override
    protected void setIntervenerTwoSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv2CorrespDocsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv3CorrespDocsShared();
    }

    @Override
    protected void setIntervenerThreeSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv3CorrespDocsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv4CorrespDocsShared();
    }

    @Override
    protected void setIntervenerFourSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv4CorrespDocsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollShared();
    }


    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv4CorrespDocs();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv3CorrespDocs();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv2CorrespDocs();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv1CorrespDocs();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getRespCorrespondenceDocsColl();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getAppCorrespondenceDocsCollection();
    }

    @Override
    protected String getIntervenerFourCollectionCcdKey() {
        return INTERVENER_FOUR_CORRESPONDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerThreeCollectionCcdKey() {
        return INTERVENER_THREE_CORRESPONDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerTwoCollectionCcdKey() {
        return INTERVENER_TWO_CORRESPONDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerOneCollectionCcdKey() {
        return INTERVENER_ONE_CORRESPONDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getRespondentCollectionCcdKey() {
        return RESP_CORRESPONDENCE_COLLECTION.getCcdKey();
    }

    @Override
    protected String getApplicantCollectionCcdKey() {
        return APPLICANT_CORRESPONDENCE_DOC_COLLECTION.getCcdKey();
    }
}
