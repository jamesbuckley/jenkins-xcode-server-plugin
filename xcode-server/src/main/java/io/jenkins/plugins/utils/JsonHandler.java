package io.jenkins.plugins.utils;

import org.json.*;

import java.util.HashMap;
import java.util.Map;

public class JsonHandler {

    public static String getIntegrationId(String jsonResponse){
        JSONObject obj = new JSONObject(jsonResponse);
        return obj.getString("_id");
    }

    public static String getIntegrationGetCurrentStep(String jsonResponse){
        JSONObject obj = new JSONObject(jsonResponse);
        return obj.getString("currentStep");
    }

    public static Map<String, Integer> getTestResults(String jsonResponse){
        JSONObject obj = new JSONObject(jsonResponse);
        JSONObject buildSummary = obj.getJSONObject("buildResultSummary");
        Map<String, Integer> testResults = new HashMap<>();
        testResults.put("testsCount", buildSummary.getInt("testsCount"));
        testResults.put("testFailureCount", buildSummary.getInt("testFailureCount"));
        return testResults;
    }
}
