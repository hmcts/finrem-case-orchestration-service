package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CORRESPONDENCE_COLLECTION;

public class UploadedDocumentHelperTest {

    private UploadedDocumentHelper uploadedDocumentHelper;
    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, Object> caseData;
    private Map<String, Object> caseDataBefore;

    @Before
    public void setUp() {
        uploadedDocumentHelper = new UploadedDocumentHelper(mapper);

        caseData = new HashMap<>();
        caseDataBefore = new HashMap<>();
    }

    @Test
    public void givenValidCaseData_whenAddUploadDateToNewContestedDocs_thenAddDate() {
        caseData.put(APP_CORRESPONDENCE_COLLECTION, List.of(
            uploadedDocument("AppCorrespondenceOne.docx", UUID.randomUUID()),
            uploadedDocument("AppCorrespondenceTwo.docx", UUID.randomUUID())
        ));

        Map<String, Object> modifiedData =
            uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore, APP_CORRESPONDENCE_COLLECTION);

        List<ContestedUploadedDocumentData> documentData = getContestedUploadedDocs(modifiedData, APP_CORRESPONDENCE_COLLECTION);

        documentData.forEach(document ->
            assertThat(document.getUploadedCaseDocument().getCaseDocumentUploadDateTime(), is(notNullValue())));
    }

    @Test
    public void givenValidCaseData_andOldDocsInCollection_whenAddUploadDate_thenAddDateToNewDocsOnly() {
        UUID oldId = UUID.randomUUID();
        caseData.put(APP_CORRESPONDENCE_COLLECTION, List.of(
            uploadedDocument("AppCorrespondenceOne.docx", UUID.randomUUID()),
            uploadedDocument("AppCorrespondenceTwo.docx", UUID.randomUUID()),
            uploadedDocument("AppCorrespondenceOld.docx", oldId)
        ));
        caseDataBefore.put(APP_CORRESPONDENCE_COLLECTION, List.of(uploadedDocument("AppCorrespondenceOld.docx", oldId)));

        Map<String, Object> modifiedData =
            uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore, APP_CORRESPONDENCE_COLLECTION);

        List<ContestedUploadedDocumentData> documentData = getContestedUploadedDocs(modifiedData, APP_CORRESPONDENCE_COLLECTION);
        assertThat(documentData, hasSize(3));
        assertThat(documentData.stream()
            .map(ContestedUploadedDocumentData::getUploadedCaseDocument)
            .map(ContestedUploadedDocument::getCaseDocumentUploadDateTime)
            .filter(Objects::nonNull).toList(), hasSize(2));
    }

    @Test
    public void givenEmptyCollections_whenAddUploadDateToDocuments_thenHandleGracefully() {
        Map<String, Object> modifiedData =
            uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore, APP_CORRESPONDENCE_COLLECTION);

        assertThat(modifiedData, is(notNullValue()));
    }

    private ContestedUploadedDocumentData uploadedDocument(String filename, UUID id) {
        return ContestedUploadedDocumentData.builder()
            .id(id.toString())
            .uploadedCaseDocument(ContestedUploadedDocument.builder()
                .caseDocumentOther(filename)
                .build())
            .build();
    }

    private List<ContestedUploadedDocumentData> getContestedUploadedDocs(Map<String, Object> data, String documentCollection) {
        return mapper.convertValue(data.get(documentCollection), new TypeReference<>() {});
    }
}