package ibm.cn.application.client;

public class Item {
	
	// Use generated ID
    private long id;

    // Item name
    private String name;

    // Item description
    private String description;

    // Item price
    private int price;

    // Item imgAlt
    private String img_alt;

    // Item img
    private String img;

    // Item stock
    private int stock;

	public Item() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Item(long id, String name, String description, int price, String img_alt, String img, int stock) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.img_alt = img_alt;
		this.img = img;
		this.stock = stock;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getImg_alt() {
		return img_alt;
	}

	public void setImg_alt(String img_alt) {
		this.img_alt = img_alt;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}
	
	public ibm.cn.application.model.Item toModel() {
        final ibm.cn.application.model.Item newItem = new ibm.cn.application.model.Item();

        newItem.setId(this.id);
        newItem.setName(this.name);
        newItem.setDescription(this.description);
        newItem.setImg(this.img);
        newItem.setImgAlt(this.img_alt);
        newItem.setPrice(this.price);
        newItem.setStock(this.stock);

        return newItem;
    }

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price + ", img_alt="
				+ img_alt + ", img=" + img + ", stock=" + stock + "]";
	}

}
