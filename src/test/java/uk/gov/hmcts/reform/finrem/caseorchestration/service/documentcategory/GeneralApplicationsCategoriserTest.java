package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationsCategoriserTest extends BaseHandlerTestSetup {
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;

    @Mock
    FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        generalApplicationsCategoriser = new GeneralApplicationsCategoriser(featureToggleService);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    public void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.categorise(finremCaseData);
        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_1.getDocumentCategoryId()
            );
        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_3.getDocumentCategoryId()
            );
    }

    @Test
    public void testGeneralApplicationOverflowCatergory() {
        FinremCaseData finremCaseData = buildFinremCaseData11Applications();
        generalApplicationsCategoriser.categorise(finremCaseData);
        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_1.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_3.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(3).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_4.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(4).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_5.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(8).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_9.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(9).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_10.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().get(10).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_OVERFLOW.getDocumentCategoryId()
            );
    }

    @Test
    public void testUncategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(finremCaseData);
        assert finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().get(0).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );

        assert finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().get(2).getValue()
            .getGeneralApplicationDocument().getCategoryId().equals(
                DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
            );
    }

    public DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    protected FinremCaseData buildFinremCaseData() {
        GeneralApplicationsCollection firstGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection secondGeneralApplications = buildGeneralApplicationsCollection(false);
        GeneralApplicationsCollection thirdGeneralApplications = buildGeneralApplicationsCollection(true);

        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        generalApplicationsCollection.add(firstGeneralApplications);
        generalApplicationsCollection.add(secondGeneralApplications);
        generalApplicationsCollection.add(thirdGeneralApplications);

        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection)
                .appRespGeneralApplications(generalApplicationsCollection)
                .intervener1GeneralApplications(generalApplicationsCollection)
                .intervener2GeneralApplications(generalApplicationsCollection)
                .intervener3GeneralApplications(generalApplicationsCollection)
                .intervener4GeneralApplications(generalApplicationsCollection)
                .build()).build();

    }

    protected FinremCaseData buildFinremCaseData11Applications() {
        GeneralApplicationsCollection firstGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection secondGeneralApplications = buildGeneralApplicationsCollection(false);
        GeneralApplicationsCollection thirdGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection fourthGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection fifthGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection sixthGeneralApplications = buildGeneralApplicationsCollection(false);
        GeneralApplicationsCollection seventhGeneralApplications = buildGeneralApplicationsCollection(false);
        GeneralApplicationsCollection eighthGeneralApplications = buildGeneralApplicationsCollection(false);
        GeneralApplicationsCollection ninthGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection tenthGeneralApplications = buildGeneralApplicationsCollection(true);
        GeneralApplicationsCollection eleventhGeneralApplications = buildGeneralApplicationsCollection(true);

        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        generalApplicationsCollection.add(firstGeneralApplications);
        generalApplicationsCollection.add(secondGeneralApplications);
        generalApplicationsCollection.add(thirdGeneralApplications);
        generalApplicationsCollection.add(fourthGeneralApplications);
        generalApplicationsCollection.add(fifthGeneralApplications);
        generalApplicationsCollection.add(sixthGeneralApplications);
        generalApplicationsCollection.add(seventhGeneralApplications);
        generalApplicationsCollection.add(eighthGeneralApplications);
        generalApplicationsCollection.add(ninthGeneralApplications);
        generalApplicationsCollection.add(tenthGeneralApplications);
        generalApplicationsCollection.add(eleventhGeneralApplications);

        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection)
                .appRespGeneralApplications(generalApplicationsCollection)
                .intervener1GeneralApplications(generalApplicationsCollection)
                .build()).build();

    }

    private GeneralApplicationsCollection buildGeneralApplicationsCollection(Boolean isGeneralApplicationDocumentPresent) {
        GeneralApplicationItems generalApplicationItems = buildGeneralApplicationItems();
        if (isGeneralApplicationDocumentPresent) {
            generalApplicationItems = buildGeneralApplicationItemsWithGeneralApplicationDocument();
        }

        return GeneralApplicationsCollection.builder()
            .value(generalApplicationItems)
            .id(UUID.randomUUID())
            .build();
    }

    private GeneralApplicationItems buildGeneralApplicationItems() {
        return GeneralApplicationItems.builder()
            .generalApplicationSender(buildDynamicIntervenerList())
            .generalApplicationCreatedBy("Claire Mumford")
            .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
            .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                LocalDate.of(2022, 8, 2))
            .build();
    }

    private GeneralApplicationItems buildGeneralApplicationItemsWithGeneralApplicationDocument() {
        return GeneralApplicationItems.builder()
            .generalApplicationSender(buildDynamicIntervenerList())
            .generalApplicationCreatedBy("Claire Mumford")
            .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
            .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                LocalDate.of(2022, 8, 2))
            .generalApplicationDocument(buildGeneralApplicationDocument())
            .build();
    }

    private CaseDocument buildGeneralApplicationDocument() {
        String doc_name = RandomStringUtils.random(10, true, false);
        return CaseDocument.builder().documentFilename(doc_name + ".pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
    }
}
