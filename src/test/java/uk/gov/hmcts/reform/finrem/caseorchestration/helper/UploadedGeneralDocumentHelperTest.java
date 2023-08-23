package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_UPLOADED_DOCUMENTS;

public class UploadedGeneralDocumentHelperTest {

    private UploadedGeneralDocumentService uploadedGeneralDocumentHelper;

    private Map<String, Object> caseData;
    private Map<String, Object> caseDataBefore;

    @Before
    public void setUp() {
        uploadedGeneralDocumentHelper = new UploadedGeneralDocumentService(new ObjectMapper());

        caseData = new HashMap<>();
        caseDataBefore = new HashMap<>();
    }

    @Test
    public void givenNewGeneralDocsAdded_whenUploadGeneralDocs_thenAddDate() {
        caseData.put(GENERAL_UPLOADED_DOCUMENTS, List.of(
            generalDocument("ApplicantLetter.docx", UUID.randomUUID()),
            generalDocument("RespondentLetter.docx", UUID.randomUUID())
        ));

        Map<String, Object> processedCaseData =
            uploadedGeneralDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore, GENERAL_UPLOADED_DOCUMENTS);

        List<GeneralUploadedDocumentData> generalDocuments = getGeneralDocuments(processedCaseData);

        generalDocuments.forEach(document ->
            assertThat(document.getGeneralUploadedDocument().getGeneralDocumentUploadDateTime(), is(notNullValue())));
    }

    @Test
    public void givenNewGeneralDocs_andPreviousDocsInCollection_whenUploadGeneralDocs_thenAddDateToNewDocs() {
        UUID oldId = UUID.randomUUID();
        caseData.put(GENERAL_UPLOADED_DOCUMENTS, List.of(
            generalDocument("ApplicantLetter.docx", UUID.randomUUID()),
            generalDocument("RespondentLetter.docx", UUID.randomUUID()),
            generalDocument("CaseLetter.docx", oldId)
        ));
        caseDataBefore.put(GENERAL_UPLOADED_DOCUMENTS, List.of(generalDocument("CaseLetter.docx", oldId)));

        Map<String, Object> processedCaseData =
            uploadedGeneralDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore, GENERAL_UPLOADED_DOCUMENTS);

        List<GeneralUploadedDocumentData> generalDocuments = getGeneralDocuments(processedCaseData);
        assertThat(generalDocuments, hasSize(3));
        assertThat(generalDocuments.stream()
            .map(GeneralUploadedDocumentData::getGeneralUploadedDocument)
            .map(GeneralUploadedDocument::getGeneralDocumentUploadDateTime)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()), hasSize(2));
    }

    @Test
    public void givenEmptyCollection_whenUploadGeneralDocs_thenHandleGracefully() {
        Map<String, Object> processedCaseData =
            uploadedGeneralDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore, GENERAL_UPLOADED_DOCUMENTS);

        assertThat(processedCaseData, is(notNullValue()));
    }

    private GeneralUploadedDocumentData generalDocument(String fileName, UUID id) {
        return GeneralUploadedDocumentData.builder()
            .id(id.toString())
            .generalUploadedDocument(GeneralUploadedDocument.builder()
                .documentFileName(fileName)
                .build())
            .build();
    }

    private List<GeneralUploadedDocumentData> getGeneralDocuments(Map<String, Object> data) {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .convertValue(data.get(GENERAL_UPLOADED_DOCUMENTS), new TypeReference<>() {});
    }
}