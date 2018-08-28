package io.jenkins.plugins.sample;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HelloWorldBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String botName = "Bobby";
    final String xcodeServerUrl = "localhost.james";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new HelloWorldBuilder(botName, xcodeServerUrl));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new HelloWorldBuilder(botName, xcodeServerUrl), project.getBuildersList().get(0));
    }

//    @Test
//    public void testConfigRoundtripFrench() throws Exception {
//        FreeStyleProject project = jenkins.createFreeStyleProject();
//        HelloWorldBuilder builder = new HelloWorldBuilder(botName, xcodeServerUrl);
//        builder.setUseFrench(true);
//        project.getBuildersList().add(builder);
//        project = jenkins.configRoundtrip(project);
//
//        HelloWorldBuilder lhs = new HelloWorldBuilder(botName, xcodeServerUrl);
//        lhs.setUseFrench(true);
//        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
//    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        HelloWorldBuilder builder = new HelloWorldBuilder(botName, xcodeServerUrl);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + botName, build);
    }

//    @Test
//    public void testBuildFrench() throws Exception {
//
//        FreeStyleProject project = jenkins.createFreeStyleProject();
//        HelloWorldBuilder builder = new HelloWorldBuilder(botName, xcodeServerUrl);
//        builder.setUseFrench(true);
//        project.getBuildersList().add(builder);
//
//        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
//        jenkins.assertLogContains("Bonjour, " + botName, build);
//    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  greet '" + botName + "'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Hello, " + botName + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}