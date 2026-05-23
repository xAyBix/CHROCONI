package ma.aybi.chroconi.github;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.jgit.api.Git;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import ma.aybi.chroconi.SetupActivity;

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
    public static void createRepository () {

    }
    public static void push () {

    }
    public static void cloneRepository () {

    }
    public static void deleteRepository () {

    }

}
