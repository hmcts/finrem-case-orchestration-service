package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

class GeneralApplicationsCategoriserTest extends BaseHandlerTestSetup {
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;

    @BeforeEach
    void setUp() {
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        generalApplicationsCategoriser = new GeneralApplicationsCategoriser(featureToggleService);
    }

    @Test
    void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.categorise(finremCaseData);

        List<GeneralApplicationsCollection> generalApplications = finremCaseData.getGeneralApplicationWrapper()
            .getGeneralApplications();

        assertThat(generalApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_1.getDocumentCategoryId());
        assertThat(generalApplications.get(2).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_3.getDocumentCategoryId());
    }

    @Test
    void testGeneralApplicationOverflowCatergory() {
        FinremCaseData finremCaseData = buildFinremCaseData11Applications();
        generalApplicationsCategoriser.categorise(finremCaseData);

        List<GeneralApplicationsCollection> generalApplications = finremCaseData.getGeneralApplicationWrapper()
            .getGeneralApplications();

        assertThat(generalApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_1.getDocumentCategoryId());
        assertThat(generalApplications.get(1).getValue().getGeneralApplicationDraftOrder().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_2.getDocumentCategoryId());
        assertThat(generalApplications.get(2).getValue().getGeneralApplicationDirectionsDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_3.getDocumentCategoryId());
        assertThat(generalApplications.get(3).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_4.getDocumentCategoryId());
        assertThat(generalApplications.get(4).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_5.getDocumentCategoryId());
        assertThat(generalApplications.get(8).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_9.getDocumentCategoryId());
        assertThat(generalApplications.get(9).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_10.getDocumentCategoryId());
        assertThat(generalApplications.get(10).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.APPLICATIONS_OTHER_APPLICATION_OVERFLOW.getDocumentCategoryId());
    }

    @Test
    void testUncategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(finremCaseData);

        List<GeneralApplicationsCollection> appRespGeneralApplications = finremCaseData.getGeneralApplicationWrapper()
            .getAppRespGeneralApplications();
        assertThat(appRespGeneralApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId());
        assertThat(appRespGeneralApplications.get(2).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId());

        List<GeneralApplicationsCollection> intervener1GeneralApplications = finremCaseData
            .getGeneralApplicationWrapper().getIntervener1GeneralApplications();
        assertThat(intervener1GeneralApplications.get(0).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId());
        assertThat(intervener1GeneralApplications.get(2).getValue().getGeneralApplicationDocument().getCategoryId())
            .isEqualTo(DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId());

    }

    @Test
    void testUncategoriseDuplicatedCollections() {
        FinremCaseData finremCaseData = buildFinremCaseDataNullCollections();
        generalApplicationsCategoriser.uncategoriseDuplicatedCollections(finremCaseData);
        assertThat(finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications()).isNull();
        assertThat(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders()).isNull();
    }

    public DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(
            getDynamicListElement(APPLICANT, APPLICANT),
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

    protected FinremCaseData buildFinremCaseDataNullCollections() {
        return FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(null)
                .appRespGeneralApplications(null)
                .intervener1GeneralApplications(null)
                .intervener2GeneralApplications(null)
                .intervener3GeneralApplications(null)
                .intervener4GeneralApplications(null)
                .generalApplicationIntvrOrders(null)
                .build()).build();
    }

    protected FinremCaseData buildFinremCaseData() {
        GeneralApplicationsCollection firstGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection secondGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection thirdGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);

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
                .generalApplicationIntvrOrders(generalApplicationsCollection)
                .build()).build();
    }

    protected FinremCaseData buildFinremCaseData11Applications() {
        GeneralApplicationsCollection firstGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection secondGeneralApplications = buildGeneralApplicationsCollection(
            false, true, false);
        GeneralApplicationsCollection thirdGeneralApplications = buildGeneralApplicationsCollection(
            false, false, true);
        GeneralApplicationsCollection fourthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection fifthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection sixthGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection seventhGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection eighthGeneralApplications = buildGeneralApplicationsCollection(
            false, false, false);
        GeneralApplicationsCollection ninthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection tenthGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);
        GeneralApplicationsCollection eleventhGeneralApplications = buildGeneralApplicationsCollection(
            true, false, false);

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

    private GeneralApplicationsCollection buildGeneralApplicationsCollection(
        Boolean isGeneralApplicationDocumentPresent,
        Boolean isGeneralApplicationDraftOrderPresent,
        Boolean isGeneralApplicationDirectionsPresent) {

        GeneralApplicationItems generalApplicationItems = buildGeneralApplicationItems();

        if (isGeneralApplicationDocumentPresent) {
            generalApplicationItems = buildGeneralApplicationItemsWithGeneralApplicationDocument();
        } else if (isGeneralApplicationDraftOrderPresent) {
            generalApplicationItems = buildGeneralApplicationItemsWithGeneralApplicationDarftOrder();
        } else if (isGeneralApplicationDirectionsPresent) {
            generalApplicationItems = buildGeneralApplicationItemsWithGeneralApplicationDirections();
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

    private GeneralApplicationItems buildGeneralApplicationItemsWithGeneralApplicationDarftOrder() {
        return GeneralApplicationItems.builder()
            .generalApplicationSender(buildDynamicIntervenerList())
            .generalApplicationCreatedBy("Claire Mumford")
            .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
            .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                LocalDate.of(2022, 8, 2))
            .generalApplicationDraftOrder(buildGeneralApplicationDocument())
            .build();
    }

    private GeneralApplicationItems buildGeneralApplicationItemsWithGeneralApplicationDirections() {
        return GeneralApplicationItems.builder()
            .generalApplicationSender(buildDynamicIntervenerList())
            .generalApplicationCreatedBy("Claire Mumford")
            .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
            .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                LocalDate.of(2022, 8, 2))
            .generalApplicationDirectionsDocument(buildGeneralApplicationDocument())
            .build();
    }

    private CaseDocument buildGeneralApplicationDocument() {
        String docName = RandomStringUtils.random(10, true, false);
        return CaseDocument.builder().documentFilename(docName + ".pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
    }
}
