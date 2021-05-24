package cn.edu.pku.utils;

import cn.edu.pku.adapter.lifecycle.GoogleActivity;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.cloudfunctions.v1.CloudFunctionsScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CredentialsProvider {
    /** Directory to store user credentials. */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/gcf");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application, but for this sample, it's only useful
     * for saving credentials across runs.
     */
    private static FileDataStoreFactory dataStoreFactory;


    static String cpath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/src/main/shell/client_secrets.json";

    public static GoogleCredentials authorize(){
        GoogleCredentials credentials = null;
        ArrayList scopes = new ArrayList();
        scopes.add("https://www.googleapis.com/auth/cloud-platform");
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(cpath))
//                    .createScoped(CloudFunctionsScopes.all());
                    .createScoped(scopes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return credentials;
        }
    }

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * <p>If you plan to run on AppEngine or Compute Engine, consider instead
     * {@link GoogleCredential#getApplicationDefault()}, which will use the ambient credentials
     * for the project's service-account.
     */
    public static Credential authorize(HttpTransport httpTransport, JsonFactory jsonFactory)
            throws IOException {
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(new FileInputStream("/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/src/main/shell/client_secrets.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/"
                            + "credential into storage-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets,
                CloudFunctionsScopes.all()).setDataStoreFactory(
                dataStoreFactory).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("liuyivector@gmail.com");
    }
}
