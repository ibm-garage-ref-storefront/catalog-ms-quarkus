package ibm.cn.application.model;

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
    private String imgAlt;

    // Item img
    private String img;

    // Item stock
    private int stock;

	public Item() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Item(long id, String name, String description, int price, String imgAlt, String img, int stock) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imgAlt = imgAlt;
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

	public String getImgAlt() {
		return imgAlt;
	}

	public void setImgAlt(String imgAlt) {
		this.imgAlt = imgAlt;
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

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price + ", imgAlt="
				+ imgAlt + ", img=" + img + ", stock=" + stock + "]";
	}

}
