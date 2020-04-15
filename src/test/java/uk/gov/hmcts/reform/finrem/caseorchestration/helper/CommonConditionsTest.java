package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isAmendedConsentOrderType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isApplicantRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.CommonConditions.isRespondentRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;

public class CommonConditionsTest {

    @Test
    public void isApplicantRepresentedShouldReturnTrueWhenApplicantRepresentedIsYes() {
        assertThat(isApplicantRepresented(createCaseDataApplRepresented(YES_VALUE)), is(true));
    }

    @Test
    public void isApplicantRepresentedShouldReturnFalse() {
        asList(
                NO_VALUE,
                "",
                null,
                "this is some random string, that doesn't make any sense"
        ).forEach(value -> assertThat(isApplicantRepresented(createCaseDataApplRepresented(value)), is(false)));
    }

    @Test
    public void isRespondentRepresentedShouldReturnTrueWhenRepresentedSolicitorIsNotEmpty() {
        asList("John Wayne", "     ", "234@#$@$@#REWF#@REWFR@#")
            .forEach(value -> assertThat(
                isRespondentRepresented(createCaseDataRespRepresented(value)),
                is(true))
            );
    }

    @Test
    public void isRespondentRepresentedShouldReturnFalse() {
        asList("", null)
            .forEach(value -> assertThat(
                isRespondentRepresented(createCaseDataRespRepresented(value)),
                is(false))
            );
    }

    @Test
    public void isNotEmptyShouldReturnTrueWhenPopulated() {
        asList(
                YES_VALUE,
                "    ",
                "any value makes it not empty",
                "1234",
                "@#$R@#F@$T"
        ).forEach(value -> assertThat(
                isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(value)),
                is(true))
        );
    }

    @Test
    public void isNotEmptyShouldReturnFalseWhenEmptyMap() {
        assertThat(
                isNotEmpty(APPLICANT_REPRESENTED, ImmutableMap.of()),
                is(false)
        );
    }

    @Test
    public void isNotEmptyShouldReturnFalseWhenFieldIsEmpty() {
        assertThat(
                isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(StringUtil.EMPTY_STRING)),
                is(false)
        );
    }

    @Test(expected = NullPointerException.class)
    public void isNotEmptyShouldThrowNullPointerException() {
        isNotEmpty(APPLICANT_REPRESENTED, null);
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnFalseForDefaultEmptyObject() {
        RespondToOrderData data = new RespondToOrderData();
        data.setRespondToOrder(new RespondToOrder());

        assertThat(isAmendedConsentOrderType(data), is(false));
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnFalseWhenDocumentTypeIsNotAmendedConsentOrder() {
        assertThat(isAmendedConsentOrderType(getRespondToOrderData("ble ble ble")), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void isAmendedConsentOrderTypeShouldThrowNullPointerException() {
        isAmendedConsentOrderType(null);
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnTrueWhenDocumentTypeIsAmendedConsentOrder() {
        assertThat(isAmendedConsentOrderType(getRespondToOrderData(AMENDED_CONSENT_ORDER)), is(true));
    }

    private static RespondToOrderData getRespondToOrderData(String s) {
        RespondToOrderData data = new RespondToOrderData();
        RespondToOrder respondToOrder = new RespondToOrder();
        respondToOrder.setDocumentType(s);
        data.setRespondToOrder(respondToOrder);

        return data;
    }


    private static Map<String, Object> createCaseDataApplRepresented(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(APPLICANT_REPRESENTED, value);

        return data;
    }

    private static Map<String, Object> createCaseDataRespRepresented(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(RESP_SOLICITOR_NAME, value);

        return data;
    }
}
