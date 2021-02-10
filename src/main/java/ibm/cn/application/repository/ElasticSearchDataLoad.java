package ibm.cn.application.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.config.ConfigProvider;
import org.json.JSONObject;

import ibm.cn.application.model.Item;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ElasticSearchDataLoad {
	
	private ItemService itemService = new ItemService();
	
	// Optional
    private String user;

    //Optional
    private String password;
    
    private OkHttpClient client = new OkHttpClient();
    
    private String url = ConfigProvider.getConfig().getValue("quarkus.elasticsearch.hosts", String.class);
    private String index = ConfigProvider.getConfig().getValue("elasticsearch.index", String.class);
    private String doc_type = ConfigProvider.getConfig().getValue("elasticsearch.doc_type", String.class);
    
    private Map<Long, Item> getAllRowsFromCache() throws IOException {

        final List<Item> allItems = itemService.findAll();

        // hash the items by Id
        final Map<Long, Item> itemMap = new HashMap<Long, Item>();
        for (final Item item : allItems) {
            itemMap.put(item.getId(), item);
        }

        return itemMap;

    }
    
    // load multi-rows
    public void loadRows(List<Item> items) throws IOException {

        // convert Item to JSONArray
    	Jsonb jsonb = JsonbBuilder.create();

        Map<Long, Item> allItemMap = getAllRowsFromCache();
     
        final StringBuilder sb = new StringBuilder();

        // convert to a bulk update
        // { "index": {"_index": "<index>", "_type": "<type>", "_id": "<itemId", "_retry_on_conflict": "3" } }
        // { "doc": <document> }
        for (final Item item : items) {
            if (allItemMap.containsKey(item.getId()) &&
                    (allItemMap.remove(item.getId()).equals(item))) {
                // the item already exists, and it's exactly the same.  continue
                continue;
            }

            sb.append("{ \"index\": { \"_index\": \"" + index + "\", \"_type\": \"" + doc_type + "\", \"_id\": \"" + item.getId() + "\", \"_retry_on_conflict\": \"3\" } }\n");
        
            String jsonString;
            jsonString = jsonb.toJson(item);

            System.out.println("Adding/updating item: \n" + item.getId() + ": " + jsonString);
            sb.append(jsonString + "\n");
        }

        // everything left in allItemMap is stuff that is still in cache that we should remove
        for (final Item item : allItemMap.values()) {
            System.out.println("Deleting item: \n" + item.getId());
            sb.append("{ \"delete\": { \"_index\": \"" + index + "\", \"_type\": \"" + doc_type + "\", \"_id\": \"" + item.getId() + "\", \"_retry_on_conflict\": \"3\" } }\n");
        }

        try {

            if (sb.toString().length() == 0) {
                return;
            }

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, sb.toString());

            // Build URL
            String url = String.format("%s/_bulk", this.url);
            Request.Builder builder = new Request.Builder().url(url)
                    .post(body)
                    .addHeader("content-type", "application/json");

            if (user != null && !user.equals("") && password != null && !password.equals("")) {
                builder.addHeader("Authorization", Credentials.basic(user, password));
            }

            Request request = builder.build();

            Response response = client.newCall(request).execute();
            String resp_string = response.body().string();
            System.out.println("resp_string: \n" + resp_string);
            JSONObject resp = new JSONObject(resp_string);
            
            boolean errors = resp.getBoolean("errors");

            if (errors) {
                System.err.println("Error(s) were found with bulk items update: " + resp_string);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
