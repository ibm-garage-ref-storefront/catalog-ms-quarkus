package ibm.cn.application;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ibm.cn.application.client.InventoryServiceClient;
import ibm.cn.application.client.Item;
import ibm.cn.application.repository.ElasticSearchDataLoad;
import ibm.cn.application.repository.ItemService;

public class InventoryRefreshTask extends Thread {
	
	@RestClient
	@Inject
	private InventoryServiceClient invClient;
	
	public InventoryRefreshTask(InventoryServiceClient invClient) {
        this.invClient = invClient;
    }

    private static final int INVENTORY_REFRESH_SLEEP_TIME_MS = 300000;

    private ElasticSearchDataLoad elasticSearch = new ElasticSearchDataLoad();

    @Inject
    ItemService itemsRepo;

    public void run() {
        while (true) {
            try {
                System.out.println("Querying Inventory Service for all items ...");
                final List<Item> allItems = invClient.getAllItems();
                final List<ibm.cn.application.model.Item> modelItems = new ArrayList<ibm.cn.application.model.Item>(allItems.size());

                for (final Item item : allItems) {
                    modelItems.add(item.toModel());
                }

                elasticSearch.loadRows(modelItems);
                System.out.println("rows loaded");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(INVENTORY_REFRESH_SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}
