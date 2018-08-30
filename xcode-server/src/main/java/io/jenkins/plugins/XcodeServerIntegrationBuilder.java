package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.utils.HttpBuilder;
import io.jenkins.plugins.utils.JsonHandler;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

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
        this.xcodeServerUrl = xcodeServerUrl;
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

        String requestUrl = xcodeServerUrl+"/bots/"+botName+"/integrations";
        String integrationId = JsonHandler.getIntegrationId(HttpBuilder.executePostRequest(requestUrl));
        boolean integrationComplete = false;
        String pollUrl = xcodeServerUrl+"/integrations/"+integrationId;
        while (!integrationComplete){
            Thread.sleep(5000);
            String currentStep = JsonHandler.getIntegrationGetCurrentStep(HttpBuilder.executeGetRequest(pollUrl));
            listener.getLogger().println("Reponse from Xcode Server...");
            listener.getLogger().println("The current step is: "+ currentStep);
            if(currentStep.equals("completed")){integrationComplete = true;}
        }

        Map<String, Integer> testResults = JsonHandler.getTestResults(HttpBuilder.executeGetRequest(pollUrl));
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

    }
}
