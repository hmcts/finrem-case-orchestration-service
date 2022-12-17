package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocumentCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

public class UploadedConfidentialDocumentHelperTest {

    private UploadedConfidentialDocumentService uploadedConfidentialDocumentHelper;
    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, Object> caseData;
    private Map<String, Object> caseDataBefore;

    @Before
    public void setUp() {
        uploadedConfidentialDocumentHelper = new UploadedConfidentialDocumentService(mapper);

        caseData = new HashMap<>();
        caseDataBefore = new HashMap<>();
    }

    @Test
    public void givenValidCaseData_whenAddUploadDateToConfidentialDocs_thenAddDate() {
        caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, List.of(
            confidentialDocument("AppConfidentialOne.docx", UUID.randomUUID()),
            confidentialDocument("AppConfidentialTwo.docx", UUID.randomUUID())
        ));

        Map<String, Object> modifiedData = uploadedConfidentialDocumentHelper.addUploadDateToNewDocuments(caseData,
            caseDataBefore,
            CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);

        List<UploadConfidentialDocumentCollection> documentData =
            getContestedUploadedDocs(modifiedData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);

        documentData.forEach(document ->
            assertThat(document.getValue().getConfidentialDocumentUploadDateTime(), is(notNullValue())));
    }

    @Test
    public void givenValidCaseData_andOldDocsInCollection_whenAddUploadDate_thenAddToNewDocsOnly() {
        UUID oldId = UUID.randomUUID();
        caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, List.of(
            confidentialDocument("AppConfidentialOne.docx", UUID.randomUUID()),
            confidentialDocument("AppConfidentialTwo.docx", UUID.randomUUID()),
            confidentialDocument("AppConfidentialOld.docx", oldId)
        ));
        caseDataBefore.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, List.of(
            confidentialDocument("AppConfidentialOld.docx", oldId)
        ));

        Map<String, Object> modifiedData = uploadedConfidentialDocumentHelper.addUploadDateToNewDocuments(caseData,
            caseDataBefore,
            CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);

        List<UploadConfidentialDocumentCollection> documentData = getContestedUploadedDocs(modifiedData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);

        assertThat(documentData, hasSize(3));
        assertThat(documentData.stream()
            .map(UploadConfidentialDocumentCollection::getValue)
            .map(UploadConfidentialDocument::getConfidentialDocumentUploadDateTime)
            .filter(Objects::nonNull)
            .toList(), hasSize(2));
    }

    @Test
    public void givenEmptyCollection_whenAddUploadDate_thenHandleGracefully() {
        Map<String, Object> modifiedData = uploadedConfidentialDocumentHelper.addUploadDateToNewDocuments(caseData,
            caseDataBefore, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);

        assertThat(modifiedData, is(notNullValue()));
    }

    private UploadConfidentialDocumentCollection confidentialDocument(String filename, UUID id) {
        return UploadConfidentialDocumentCollection.builder()
            .id(id.toString())
            .value(UploadConfidentialDocument.builder()
                .documentFileName(filename)
                .build())
            .build();
    }

    private List<UploadConfidentialDocumentCollection> getContestedUploadedDocs(Map<String, Object> data, String documentCollection) {
        return mapper.convertValue(data.get(documentCollection), new TypeReference<>() {});
    }
}