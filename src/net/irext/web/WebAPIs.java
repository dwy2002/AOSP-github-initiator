package net.irext.web;

import net.irext.web.WebAPICallbacks.*;
import net.irext.web.request.CreateFileRequest;
import net.irext.web.request.CreateRepoRequest;
import net.irext.web.request.DeleteRepoRequest;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Filename:       WebAPIs.java
 * Revised:        Date: 2017-11-13
 * Revision:       Revision: 1.0
 * <p>
 * Description:    HTTP Request initializer
 * <p>
 * Revision log:
 * 2017-11-13: created by strawmanbobi
 */
public class WebAPIs {

    @SuppressWarnings("all")
    private static final String TAG = WebAPIs.class.getSimpleName();

    private static WebAPIs mInstance = null;

    private OkHttpClient mHttpClient;

    private WebAPIs() {
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    private static void initializeInstance() {
        mInstance = new WebAPIs();
    }

    @SuppressWarnings("unused")
    public static WebAPIs getInstance() {
        if (null == mInstance) {
            initializeInstance();
        }
        return mInstance;
    }

    private String postToServer(String url, String json) throws IOException {
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        return response.body().string();
    }

    private String deleteToServer(String url, String json) throws IOException {
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        return response.body().string();
    }

    private String putToServer(String url, String json) throws IOException {
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        return response.body().string();
    }

    @SuppressWarnings("unused")
    public void createRepo(String name, String description, String githubBase, String orgName, String accessToken,
                           CreateRepoCallback createRepoCallback) {
        CreateRepoRequest createRepoRequest = new CreateRepoRequest();
        createRepoRequest.setName(name);
        createRepoRequest.setDescription(description);
        String bodyJson = createRepoRequest.toJson();
        System.out.println(bodyJson);
        System.out.println();
        try {
            // https://[githubBase]/api/v3/orgs/[orgName]/repos?access_token=xxx
            String url = "https://" + githubBase + "/api/v3/orgs/" + orgName + "/repos?access_token=" + accessToken;
            String response = postToServer(url, bodyJson);
            createRepoCallback.onCreateRepoSuccess(response);
        } catch (Exception e) {
            e.printStackTrace();
            createRepoCallback.onCreateRepoError();
        }
    }

    @SuppressWarnings("unused")
    public void deleteRepo(String name, String githubBase, String orgName, String accessToken,
                           DeleteRepoCallback deleteRepoCallback) {
        DeleteRepoRequest deleteRepoRequest = new DeleteRepoRequest();
        String bodyJson = deleteRepoRequest.toJson();
        try {
            // https://[githubBase]/api/v3/repos/[orgName]/test-1?access_token=xxx
            String url = "https://" + githubBase + "/api/v3/repos/" + orgName + "/" + name +
                    "?access_token=" + accessToken;
            String response = deleteToServer(url, bodyJson);
            deleteRepoCallback.onDeleteRepoSuccess(response);
        } catch (Exception e) {
            e.printStackTrace();
            deleteRepoCallback.onDeleteRepoError();
        }
    }

    @SuppressWarnings("unused")
    public void createFile(String repoName, String githubBase, String orgName, String accessToken,
                           String filePath, String message, String content, CreateFileCallback createFileCallback) {
        CreateFileRequest createFileRequest = new CreateFileRequest();
        createFileRequest.setMessage(message);
        createFileRequest.setContent(content);
        String bodyJson = createFileRequest.toJson();
        try {
            // https://[githubBase]/api/v3/repos/[orgName]/test-1/contents/empty?access_token=xxx
            String url = "https://" + githubBase + "/api/v3/repos/" + orgName + "/" + repoName + "/contents/" +
                    filePath + "?access_token=" + accessToken;
            String response = putToServer(url, bodyJson);
            createFileCallback.onCreateFileSuccess(response);
        } catch (Exception e) {
            e.printStackTrace();
            createFileCallback.onCreateFileError();
        }
    }
}
