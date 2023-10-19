package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_OTHER_COLLECTION;

@Component
public class OtherDocumentSharer extends DocumentSharer {

    @Override
    protected void setRespondentSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setRespOtherCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getRespOtherCollectionShared();
    }


    @Override
    protected void setApplicantSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setAppOtherCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv1OtherShared();
    }

    @Override
    protected void setIntervenerOneSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv1OtherShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv2OtherShared();
    }

    @Override
    protected void setIntervenerTwoSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv2OtherShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv3OtherShared();
    }

    @Override
    protected void setIntervenerThreeSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv3OtherShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv4OtherShared();
    }

    @Override
    protected void setIntervenerFourSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv4OtherShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppOtherCollectionShared();
    }


    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv4Other();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv3Other();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv2Other();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv1Other();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getRespOtherCollection();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getAppOtherCollection();
    }

    @Override
    protected String getIntervenerFourCollectionCcdKey() {
        return INTERVENER_FOUR_OTHER_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerThreeCollectionCcdKey() {
        return INTERVENER_THREE_OTHER_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerTwoCollectionCcdKey() {
        return INTERVENER_TWO_OTHER_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerOneCollectionCcdKey() {
        return INTERVENER_ONE_OTHER_COLLECTION.getCcdKey();
    }

    @Override
    protected String getRespondentCollectionCcdKey() {
        return RESP_OTHER_COLLECTION.getCcdKey();
    }

    @Override
    protected String getApplicantCollectionCcdKey() {
        return APP_OTHER_COLLECTION.getCcdKey();
    }
}
