package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerFourToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerThreeToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerTwoToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.paymentDocumentCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

@RunWith(MockitoJUnitRunner.class)
public class FormCandGCorresponderTest extends HearingCorrespondenceBaseTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private GenericDocumentService service;
    private FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
    private IntervenerOneToIntervenerDetailsMapper intervenerOneDetailsMapper;
    private IntervenerTwoToIntervenerDetailsMapper intervenerTwoDetailsMapper;
    private IntervenerThreeToIntervenerDetailsMapper intervenerThreeDetailsMapper;
    private IntervenerFourToIntervenerDetailsMapper intervenerFourDetailsMapper;
    private static final String DATE_OF_HEARING = "2019-01-01";

    @Before
    public void setUp() throws Exception {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        caseDetails = caseDetails(NO_VALUE);
        applicantAndRespondentMultiLetterCorresponder =
            new FormCandGCorresponder(bulkPrintService, notificationService,
                new DocumentHelper(objectMapper, new CaseDataService(objectMapper), service,finremCaseDetailsMapper, intervenerOneDetailsMapper,
                    intervenerTwoDetailsMapper, intervenerThreeDetailsMapper, intervenerFourDetailsMapper), objectMapper);
    }

    @Test
    public void getDocumentsToPrint() {
        List<BulkPrintDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getDocumentsToPrint(caseDetails);
        assertEquals(5, documentsToPrint.size());
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(FAST_TRACK_DECISION, isFastTrackDecision);
        caseData.put(HEARING_DATE, DATE_OF_HEARING);
        caseData.put(FORM_A_COLLECTION, singletonList(paymentDocumentCollection()));
        caseData.put(FORM_C, caseDocument());
        caseData.put(FORM_G, caseDocument());
        caseData.put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseData.put(HEARING_ADDITIONAL_DOC, caseDocument());

        return CaseDetails.builder().id(1234L).data(caseData).build();
    }
}