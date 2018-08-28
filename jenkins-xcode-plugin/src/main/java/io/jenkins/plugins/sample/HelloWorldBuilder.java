package io.jenkins.plugins.sample;

import com.google.gson.stream.JsonReader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Executor;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import static org.apache.http.HttpHeaders.USER_AGENT;

@SuppressFBWarnings
public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private final String botName;
    private boolean useFrench;
    private String xcodeServerUrl;

    @DataBoundConstructor
    public HelloWorldBuilder(String botName, String xcodeServerUrl) {
        this.botName = botName;
        this.xcodeServerUrl = xcodeServerUrl;
    }

    public String getBotName() {
        return botName;
    }

    public boolean isUseFrench() {
        return useFrench;
    }

    public String getXcodeServerUrl() { return  xcodeServerUrl; }

    @DataBoundSetter
    public void setUseFrench(boolean useFrench) {
        this.useFrench = useFrench;
    }

    @DataBoundSetter
    public void setXcodeServerUrl(String xcodeServerUrl){ this.xcodeServerUrl = xcodeServerUrl; }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("The URL is " + xcodeServerUrl);
        try{
            triggerIntegration(listener);
        }catch (Exception e){

        }

    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String botname, @QueryParameter String xcodeUrl)
                throws IOException, ServletException {
            if (botname.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
//            if (xcodeUrl.length() == 0)
//                return FormValidation.warning(Messages.XcodePluginBuilder_DescriptorImpl_errors_missingServer());
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

    private void triggerIntegration(TaskListener listener) throws IOException, NoSuchAlgorithmException {
        String url = xcodeServerUrl;

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());

            CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSslcontext(sc).build();
            HttpPost request = new HttpPost(url);

            // add request header
            request.addHeader("User-Agent", USER_AGENT);

            HttpResponse response = client.execute(request);

            listener.getLogger().println("\nSending 'GET' request to URL : " + url);
            listener.getLogger().println("Response Code : " +
                    response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            parseJson(result.toString());
            listener.getLogger().println(result.toString());
        }catch (Exception e) { }
    }

    private void pollXcodeServer(){
        // poll server until response arrives
    }

    private void parseJson(String jsonRespose){
        JsonReader jsonReader = new JsonReader(new StringReader(jsonRespose));
        try{
            if(jsonReader.hasNext()){

            }
        }catch (IOException e){
        }

    }

}
