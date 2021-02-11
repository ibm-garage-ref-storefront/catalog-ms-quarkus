package ibm.cn.application.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.config.ConfigProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ibm.cn.application.model.Item;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ApplicationScoped
public class ItemService {
	
	// Optional
    private String user;

    // Optional
    private String password;
	
    private String url = ConfigProvider.getConfig().getValue("quarkus.elasticsearch.hosts", String.class);
	private String index = ConfigProvider.getConfig().getValue("elasticsearch.index", String.class);
    private String doc_type = ConfigProvider.getConfig().getValue("elasticsearch.doc_type", String.class);
    
    private OkHttpClient client;

    // Constructor
    public ItemService() {
        client = new OkHttpClient();
    }
	
	// Get all rows from database
    public List<Item> findAll() {
    	List<Item> list;
        final String req_url = url + "/" + index + "/" + doc_type + "/_search?size=1000&pretty=true";
        final Response response = perform_request(req_url);

        try {
            list = getItemsFromResponse(response);
        } catch (IOException e) {
            // Just to be safe
            list = null;
        }
        return list;
    }
    
    // Search by id
    public Item findById(long id) {
    	Item item = null;
    	Jsonb jsonb = JsonbBuilder.create();
        String req_url = url + "/" + index + "/" + doc_type + "/" + id;
        System.out.println("req url"+req_url);
        Response response = perform_request(req_url);
        System.out.println("response url"+response.toString());

        try {
            JSONObject resp = new JSONObject(response.body().string());

            if (resp.has("found") && resp.getBoolean("found") == true) {
                JSONObject itm = resp.getJSONObject("_source");

                item = jsonb.fromJson(itm.toString(), Item.class);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return item;
    } 
    
    private Response perform_request(String req_url) {
        Response response;
        try {
            Request.Builder builder = new Request.Builder()
                    .url(req_url)
                    .get()
                    .addHeader("content-type", "application/json");

            if (user != null && !user.equals("") && password != null && !password.equals("")) {
                builder.addHeader("Authorization", Credentials.basic(user, password));
            }

            Request request = builder.build();

            response = client.newCall(request).execute();

        } catch (IOException e) {
            // Just to be safe
            response = null;
            e.printStackTrace();
        }

        return response;
    }

    private List<Item> getItemsFromResponse(Response response) throws JSONException, IOException {
    	Jsonb jsonb = JsonbBuilder.create();
        List<Item> list = new ArrayList<Item>();
        JSONObject resp = new JSONObject(response.body().string());
        if (!resp.has("hits")) {
            // empty cache
            return list;
        }

        JSONArray hits = resp.getJSONObject("hits").getJSONArray("hits");

        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
            Item item = jsonb.fromJson(hit.toString(), Item.class);
            list.add(item);
        }

        return list;
    }

}
