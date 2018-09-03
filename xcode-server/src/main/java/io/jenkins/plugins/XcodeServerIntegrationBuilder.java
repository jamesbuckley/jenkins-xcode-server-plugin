package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.utils.HttpBuilder;
import io.jenkins.plugins.utils.JsonHandler;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.Map;

public class XcodeServerIntegrationBuilder extends Builder implements SimpleBuildStep {

    private String xcodeServerUrl;
    private String botName;

    @DataBoundConstructor
    public XcodeServerIntegrationBuilder(String xcodeServerUrl, String botName){
        this.xcodeServerUrl = xcodeServerUrl;
        this.botName = botName;
    }

    public String getXcodeServerUrl() {
        return xcodeServerUrl;
    }

    public void setXcodeServerUrl(String xcodeServerUrl) {
        this.xcodeServerUrl = xcodeServerUrl+"/api";
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("The URL is " + xcodeServerUrl);
        listener.getLogger().println("The chosen bot is " + botName);

        String requestUrl = xcodeServerUrl+"/api/bots/"+botName+"/integrations";
        String integrationId = JsonHandler.getIntegrationId(HttpBuilder.executePostRequest(requestUrl));
        boolean integrationComplete = false;
        String pollUrl = xcodeServerUrl+"/api/integrations/"+integrationId;
        while (!integrationComplete){
            Thread.sleep(5000);
            String currentStep = JsonHandler.getIntegrationGetCurrentStep(HttpBuilder.executeGetRequestStringResponse(pollUrl));
            listener.getLogger().println("Reponse from Xcode Server...");
            listener.getLogger().println("The current step is: "+ currentStep);
            if(currentStep.equals("completed")){integrationComplete = true;}
        }

        Map<String, Integer> testResults = JsonHandler.getTestResults(HttpBuilder.executeGetRequestStringResponse(pollUrl));
        listener.getLogger().println("Total Test Count: " + testResults.get("testsCount"));
        listener.getLogger().println("Total Test Failures: "+ testResults.get("testFailureCount"));
        if((testResults.get("testFailureCount")) > 0){
            throw new RuntimeException("There are test failures - failing build");
        }
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>{

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName(){
            return "Xcode Server Integration Details";
        }

        public ListBoxModel doFillBotNameItems(@QueryParameter String xcodeServerUrl){
            ListBoxModel result = new ListBoxModel();
            if(xcodeServerUrl != null && !xcodeServerUrl.equals("")){
                String botList = xcodeServerUrl + "/api/bots/";
                Map<String, String> bots = JsonHandler.getBotList(HttpBuilder.executeGetRequestStringResponse(botList));
                for (Map.Entry<String, String> entry : bots.entrySet())
                {
                    result.add(new ListBoxModel.Option(entry.getValue(), entry.getKey()));
                }
            }
            return result;
        }

        public FormValidation doCheckXcodeServerUrl(@QueryParameter String xcodeServerUrl){
            if(xcodeServerUrl != null && xcodeServerUrl.contains("xcode")) {
                if (HttpBuilder.executeGetRequestResponseCode(xcodeServerUrl) == 200) {
                    doFillBotNameItems(xcodeServerUrl);
                    return FormValidation.ok();
                }
            }
            return FormValidation.error("URL should match https://xxx/xxx/xcode/internal and should be a valid Xcode Server");
        }

    }
}
