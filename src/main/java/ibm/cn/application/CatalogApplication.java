package ibm.cn.application;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import ibm.cn.application.client.InventoryServiceClient;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain  
public class CatalogApplication {

    public static void main(String ... args) {
        System.out.println("Running main method");
        Quarkus.run(CatalogApp.class, args); 
    }
    
    public static class CatalogApp implements QuarkusApplication {
    	
    	@Inject
        @RestClient
        private InventoryServiceClient invClient;

        @Override
        public int run(String... args) throws Exception {
        	InventoryRefreshTask inv = new InventoryRefreshTask(invClient);
            inv.start();
            Quarkus.waitForExit();
            return 0;
        }
    }
}

