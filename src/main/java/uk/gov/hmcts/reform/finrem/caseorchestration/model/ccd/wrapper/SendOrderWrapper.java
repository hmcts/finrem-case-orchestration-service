package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlPostStateOption;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOrderWrapper {

    @CCD(
            label = "Please upload any additional document",
            categoryID = "approvedOrders",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @Deprecated
    private CaseDocument additionalDocument;

    @CCD(
            label = "Please upload any additional documents",
            categoryID = "approvedOrders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private List<DocumentCollectionItem> additionalDocuments;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class})
    @TemporaryField
    private OrdersToSend ordersToSend;

    @CCD(
            label = "What state should this case move to:",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_postStateOption",
            typeParameterClass = FRFlPostStateOption.class,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private SendOrderEventPostStateOption sendOrderPostStateOption;

    /**
     * This field is no longer in use and is deprecated since new draft order flow release.
     *
     * @deprecated This field is no longer in use and is deprecated since new draft order flow release.
     */
    @CCD(label = "Select Order/Orders", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @Deprecated
    private DynamicMultiSelectList ordersToShare;

}
