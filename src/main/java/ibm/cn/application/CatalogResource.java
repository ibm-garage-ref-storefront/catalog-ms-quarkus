package ibm.cn.application;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ibm.cn.application.model.Item;
import ibm.cn.application.repository.ItemService;

@Path("/micro/items")
public class CatalogResource {
	
	@Inject
	ItemService itemsRepo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getInventory() throws IOException {
    	List<Item> items = null;
        items = itemsRepo.findAll();
        return items;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response getById(@PathParam("id") long id) throws IOException {
        final Item item = itemsRepo.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(item, MediaType.APPLICATION_JSON).build();
    }
}