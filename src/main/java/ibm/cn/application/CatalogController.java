package ibm.cn.application;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/catalog")
public class CatalogController {
	
	@GET
	@Path("/resource")
    public String getRequest() {
        return "CatalogResource response";
    }

}
