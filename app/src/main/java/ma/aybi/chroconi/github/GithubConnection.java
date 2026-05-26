package ma.aybi.chroconi.github;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.security.Encryptor;
import ma.aybi.chroconi.util.Constants;

public class GithubConnection {
    public static void testConnection(Context context,
                                      String token,
                                      BooleanCallBack callback) {

        RequestQueue volleyQueue = Volley.newRequestQueue(context);

        String url = "https://api.github.com/user";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    // HTTP 200
                    callback.onResult(true);
                },

                error -> {

                    if (error.networkResponse != null) {
                        callback.onResult(false);
                    }
                }

        ) {

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/vnd.github+json");

                return headers;
            }
        };

        volleyQueue.add(request);
    }
    public static void createRepository (Context context, String hostname, String invitedname, BooleanStringCallBack callBack) {
        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));
        String repoName = Constants.REPO_PREFIXES + UUID.randomUUID().toString() + "_" + hostname + "_" + invitedname;
        String urlRepo = "https://api.github.com/user/repos";

        JsonObjectRequest requestRepo = new JsonObjectRequest(
                Request.Method.POST,  // Changed from GET to POST
                urlRepo,
                null,

                response -> {
                    // HTTP 201 Created - Repository created successfully
                    callBack.onResult(true, repoName);
                },

                error -> {
                    if (error.networkResponse != null) {
                        // Log the error for debugging
                        String errorBody = new String(error.networkResponse.data);
                        Log.e("GitHubAPI", "Error " + error.networkResponse.statusCode + ": " + errorBody);
                        callBack.onResult(false, null);
                    } else {
                        callBack.onResult(false, null);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/vnd.github.v3+json");  // Standard GitHub API version
                headers.put("Content-Type", "application/json");  // Important: specify content type
                return headers;
            }

            @Override
            public byte[] getBody() {
                String jsonBody = String.format(
                        "{" +
                                "\"name\": \"%s\"," +
                                "\"description\": \"%s\"," +
                                "\"private\": %b," +
                                "\"auto_init\": true" +
                                "}",
                        repoName,
                        "Chroconi conversation between " + hostname + " and " + invitedname + ".",
                        true
                );
                return jsonBody.getBytes();
            }
        };


        volleyQueue.add(requestRepo);

    }

    public static void setCollab (Context context, String repoName,String hostname, String invitedname, BooleanStringCallBack callBack) {
        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));
        String urlCollab = "https://api.github.com/repos/"+ hostname + "/" + repoName +
                "/collaborators/" + invitedname;

        JsonObjectRequest requestCollab = new JsonObjectRequest(
                Request.Method.PUT,  // Use PUT to add/update collaborator
                urlCollab,
                null,

                response -> {
                    // HTTP 201 - Invitation sent successfully
                    // HTTP 204 - User already a collaborator
                    callBack.onResult(true, repoName);
                },

                error -> {
                    if (error.networkResponse != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e("GitHubAPI", "Status: " + error.networkResponse.statusCode);
                        Log.e("GitHubAPI", "Error: " + errorBody);

                        // Handle specific error cases
                        if (error.networkResponse.statusCode == 404) {
                            Log.e("GitHubAPI", "User or repository not found");
                        } else if (error.networkResponse.statusCode == 403) {
                            Log.e("GitHubAPI", "Insufficient permissions or token missing 'repo' scope");
                        }
                    }
                    callBack.onResult(false, null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/vnd.github.v3+json");
                return headers;
            }
        };
        volleyQueue.add(requestCollab);
    }

    public static void push () {

    }
    public static void cloneRepository () {

    }
    public static void deleteRepository () {

    }

}
