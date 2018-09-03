package io.jenkins.plugins.utils;

import org.json.*;

import java.util.HashMap;
import java.util.Map;

public class JsonHandler {

    public static String getIntegrationId(String jsonResponse){
        try{
            JSONObject obj = new JSONObject(jsonResponse);
            return obj.getString("_id");
        }catch (org.json.JSONException e){
            return "Unexpected response from server when getting Integration Id";
        }
    }

    public static String getIntegrationGetCurrentStep(String jsonResponse){
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            return obj.getString("currentStep");
        }catch (org.json.JSONException e){
            return "Unexpected response from server when getting Current Integration Step";
        }
    }

    public static Map<String, Integer> getTestResults(String jsonResponse){
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject buildSummary = obj.getJSONObject("buildResultSummary");
            Map<String, Integer> testResults = new HashMap<>();
            testResults.put("testsCount", buildSummary.getInt("testsCount"));
            testResults.put("testFailureCount", buildSummary.getInt("testFailureCount"));
            return testResults;
        }catch (org.json.JSONException e){
            return new HashMap<String, Integer>();
        }

    }

    public static Map<String, String> getBotList(String jsonResponse){
        try {
            Map<String, String> botDetails = new HashMap<>();
            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray bots = obj.getJSONArray("results");
            for(Object bot :bots){
                JSONObject botInstance = (JSONObject)bot;
                String id = botInstance.getString("_id");
                String name = botInstance.getString("name");
                botDetails.put(id, name);
            }
            return botDetails;
        }catch (org.json.JSONException e){
            return new HashMap<String, String>();
        }

    }
}
