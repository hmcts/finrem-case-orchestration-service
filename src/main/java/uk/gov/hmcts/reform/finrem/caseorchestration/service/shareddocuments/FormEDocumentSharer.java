package uk.gov.hmcts.reform.finrem.caseorchestration.service.shareddocuments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION;

@Component
public class FormEDocumentSharer extends DocumentSharer {

    @Autowired
    public FormEDocumentSharer(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void setRespondentSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setRespFormEExhibitsCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollectionShared();
    }

    @Override
    protected void setApplicantSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setAppFormEExhibitsCollectionShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollectionShared();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv1FormEsExhibitsShared();
    }

    @Override
    protected void setIntervenerOneSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv1FormEsExhibitsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv2FormEsExhibitsShared();
    }

    @Override
    protected void setIntervenerTwoSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv2FormEsExhibitsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv3FormEsExhibitsShared();
    }

    @Override
    protected void setIntervenerThreeSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv3FormEsExhibitsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourSharedCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getIntv4FormEsExhibitsShared();
    }

    @Override
    protected void setIntervenerFourSharedCollection(FinremCaseData caseData, List<UploadCaseDocumentCollection> list) {
        caseData.getUploadCaseDocumentWrapper().setIntv4FormEsExhibitsShared(list);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerFourCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv4FormEsExhibits();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerThreeCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv3FormEsExhibits();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerTwoCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv2FormEsExhibits();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getIntervenerOneCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getIntv1FormEsExhibits();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getRespondentCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getRespFormEExhibitsCollection();
    }

    @Override
    protected List<UploadCaseDocumentCollection> getApplicantCollection(UploadCaseDocumentWrapper documentWrapper) {
        return documentWrapper.getAppFormEExhibitsCollection();
    }

    @Override
    protected String getIntervenerFourCollectionCcdKey() {
        return INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerThreeCollectionCcdKey() {
        return INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerTwoCollectionCcdKey() {
        return INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getIntervenerOneCollectionCcdKey() {
        return INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getRespondentCollectionCcdKey() {
        return RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
    }

    @Override
    protected String getApplicantCollectionCcdKey() {
        return APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey();
    }
}
