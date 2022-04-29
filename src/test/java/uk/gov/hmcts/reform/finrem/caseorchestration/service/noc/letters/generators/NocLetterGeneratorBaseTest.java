package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;

public class NocLetterGeneratorBaseTest {

    @Mock
    protected DocumentConfiguration documentConfiguration;
    @Mock
    protected GenericDocumentService genericDocumentService;
    @Mock
    protected ObjectMapper objectMapper;

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String DOC_TEMPLATE = "docTemplate";
    protected static final String DOC_FILENAME = "docFilename";
    protected static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";

    protected Map caseData = null;
    protected CaseDetails caseDetails = null;

    protected NoticeOfChangeLetterDetails noticeOfChangeLetterDetails;
    protected Map notiicationLettersDetailsMap = null;

    @Before
    public void setUpTest() {

        caseData = Map.of(DIVORCE_CASE_NUMBER, "divCaseReference", SOLICITOR_REFERENCE,
            "solicitorReference");
        caseDetails = CaseDetails.builder().id(1234L).data(caseData).build();

        noticeOfChangeLetterDetails = NoticeOfChangeLetterDetails.builder()
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .caseNumber(caseDetails.getId().toString())
            .reference(Objects.toString(caseDetails.getData().get(SOLICITOR_REFERENCE)))
            .courtDetails(buildConsentedFrcCourtDetails())
            .addressee(Addressee.builder().formattedAddress("formattedAddress").name("addresseeName").build())
            .build();

        when(objectMapper.convertValue(noticeOfChangeLetterDetails, Map.class)).thenReturn(notiicationLettersDetailsMap);

    }

    protected void assertAndVerifyDocumentsAreGenerated(CaseDocument caseDocument) {
        assertNotNull(caseDocument);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(AUTH_TOKEN, notiicationLettersDetailsMap, DOC_TEMPLATE, DOC_FILENAME);
    }

}


