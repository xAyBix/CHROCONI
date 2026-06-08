package ma.aybi.chroconi.github;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.model.Invitation;
import ma.aybi.chroconi.security.Encryptor;
import ma.aybi.chroconi.util.Constants;

public class GithubConnection {
    public static void testConnection(Context context,
                                      String token,
                                      BooleanStringCallBack callback) {

        RequestQueue volleyQueue = Volley.newRequestQueue(context);

        String url = "https://api.github.com/user";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    // HTTP 200
                    try {
                        JSONObject json = new JSONObject(response.toString());
                        callback.onResult(true, json.getString("login"));
                    } catch (Exception e) {

                    }


                },

                error -> {

                    if (error.networkResponse != null) {
                        callback.onResult(false, null);
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
                        callBack.onResult(false, "Error " + error.networkResponse.statusCode + ": " + error.getMessage());
                    } else {
                        callBack.onResult(false, "Error: Unable to create the repository");
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
                Request.Method.PUT,
                urlCollab,
                null,

                response -> {
                    callBack.onResult(true, repoName);
                },

                error -> {
                    if (error.networkResponse != null) {
                        String errorMsg = error.getMessage();

                        if (error.networkResponse.statusCode == 404) {
                            callBack.onResult(false, "Error 404: User or repository not found");
                        } else if (error.networkResponse.statusCode == 403) {
                            callBack.onResult(false, "Error 403: Insufficient permissions or token missing 'repo' scope");
                        } else {
                            callBack.onResult(false, "Error: Unable to add collaborator");
                        }
                    }
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

    public static void getCollabInvitations (Context context, BooleanCallBack callBack) {

        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));
        String url = "https://api.github.com/user/repository_invitations";

        String ignoredString = PreferencesManager.getIgnoredInvitations(context);

        final Set<Integer> ignored = (ignoredString != null) ?
                Arrays.stream(ignoredString.split("_"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toSet())
                :
                new HashSet<>();

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    try {
                        Invitation.invitations.clear();

                        for (int i = 0 ; i < response.length() ; i++) {
                            JSONObject invitation = response.getJSONObject(i);
                            int id = invitation.getInt("id");
                            if (ignored.stream().noneMatch(inv -> inv == id)) {
                                JSONObject repoObj = invitation.getJSONObject("repository");
                                String repoName = repoObj.getString("full_name").split("/")[1];
                                if (repoName.startsWith(Constants.REPO_PREFIXES)) {
                                    JSONObject inviterObj = invitation.getJSONObject("inviter");
                                    String inviterLogin = inviterObj.getString("login");

                                    new Invitation(id, inviterLogin, inviterLogin + "/" + repoName);
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.d(">>>EXEP INV", e.getMessage());
                    }
                    callBack.onResult(true);
                },

                error -> {
                    callBack.onResult(false);
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
        volleyQueue.add(request);

    }

    public static void acceptInvitation (Context context, int id, BooleanCallBack callBack) {
        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));

        String url = "https://api.github.com/user/repository_invitations/"+String.valueOf(id);

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    callBack.onResult(true);
                },
                error -> {
                    callBack.onResult(false);
                    Log.d(">>>ERRRR", error.toString());
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

        volleyQueue.add(request);

    }

    public static void getRepoContents(Context context, String owner, String repo, ContentCallBack callBack) {
        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> callBack.onResult(true, response),
                error -> callBack.onResult(false, null)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/vnd.github.v3+json");
                return headers;
            }
        };
        volleyQueue.add(request);
    }

    public static void getFileContent(Context context, String owner, String repo, String filePath, ContentCallBack callBack) {
        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + filePath;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> callBack.onResult(true, response),
                error -> callBack.onResult(false, null)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/vnd.github.v3+json");
                return headers;
            }
        };
        volleyQueue.add(request);
    }

    public static void pushFileToRepo (Context context, String owner, String repo, String filePath, String content, BooleanCallBack callBack) {
        RequestQueue volleyQueue = Volley.newRequestQueue(context);
        String token = Encryptor.byteToString(Encryptor.decrypt(PreferencesManager.getToken(context)));

        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + filePath;

        String encodedContent = Base64.encodeToString(content.getBytes(), Base64.NO_WRAP);

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                url,
                response -> {
                    callBack.onResult(true);
                },
                error -> {
                    Log.d(">>>PUSH_ERR", error.toString());
                    callBack.onResult(false);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/vnd.github.v3+json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                String jsonBody = "{\"message\":\"Add file\",\"content\":\"" + encodedContent + "\"}";
                return jsonBody.getBytes();
            }
        };

        volleyQueue.add(request);
    }

}
