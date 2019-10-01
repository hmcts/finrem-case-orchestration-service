package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.InputStream;

public class RequestJsonUtil {


    public static DocumentContext getRequestDocument(){
        InputStream resourceAsStream = null;
        resourceAsStream = RequestJsonUtil.class.getResourceAsStream( "/json/basicCaseData.json");

        return JsonPath.parse(resourceAsStream);
    }

    public static DocumentContext getResponseDocument(){
        InputStream resourceAsStream = null;
        resourceAsStream = RequestJsonUtil.class.getResourceAsStream( "/json/basicCaseDataResponse.json");

        return JsonPath.parse(resourceAsStream);
    }



}
