package ibm.cn.application.client;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@RegisterProvider(InventoryResponseExceptionMapper.class)
public interface InventoryServiceClient {
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Item> getAllItems() throws UnknownUrlException, ServiceNotReadyException;

}
