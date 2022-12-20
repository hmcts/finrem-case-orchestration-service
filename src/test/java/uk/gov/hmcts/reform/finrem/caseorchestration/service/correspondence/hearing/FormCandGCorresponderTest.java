package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.formcandg.FormCandGApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.formcandg.FormCandGCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.formcandg.FormCandGRespondentCorresponder;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

@RunWith(MockitoJUnitRunner.class)
public class FormCandGCorresponderTest extends HearingCorrespondenceBaseTest {

    FormCandGApplicantCorresponder multiLetterOrEmailApplicantCorresponder;
    FormCandGRespondentCorresponder multiLetterOrEmailRespondentCorresponde;

    private static final String DATE_OF_HEARING = "2019-01-01";

    @Before
    public void setUp() throws Exception {
        caseDetails = caseDetails(NO_VALUE);
        multiLetterOrEmailApplicantCorresponder =
            new FormCandGApplicantCorresponder(notificationService, bulkPrintService, documentHelper, new ObjectMapper());

        multiLetterOrEmailRespondentCorresponde =
            new FormCandGRespondentCorresponder(notificationService, bulkPrintService, documentHelper, new ObjectMapper());

        applicantAndRespondentMultiLetterCorresponder = new FormCandGCorresponder(
            multiLetterOrEmailApplicantCorresponder,
            multiLetterOrEmailRespondentCorresponde);
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(FAST_TRACK_DECISION, isFastTrackDecision);
        caseData.put(HEARING_DATE, DATE_OF_HEARING);
        caseData.put(FORM_A_COLLECTION, singletonList(pensionDocumentData()));
        caseData.put(FORM_C, caseDocument());
        caseData.put(FORM_G, caseDocument());
        caseData.put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseData.put(HEARING_ADDITIONAL_DOC, caseDocument());

        return CaseDetails.builder().id(1234L).data(caseData).build();
    }
}