package Luxora.Luxora;

public class CartItem {

    private final int    cartID;
    private final String userEmail;
    private final int    productID;
    private       int    quantity;

    // Joined from PRODUCTS
    private final String productName;
    private final double productPrice;
    private final String productImages;
    private final int    productStock;

    public CartItem(int cartID, String userEmail, int productID, int quantity,
                    String productName, double productPrice,
                    String productImages, int productStock) {
        this.cartID        = cartID;
        this.userEmail     = userEmail;
        this.productID     = productID;
        this.quantity      = quantity;
        this.productName   = productName;
        this.productPrice  = productPrice;
        this.productImages = productImages;
        this.productStock  = productStock;
    }

    public int    getCartID()        { return cartID; }
    public String getUserEmail()     { return userEmail; }
    public int    getProductID()     { return productID; }
    public int    getQuantity()      { return quantity; }
    public void   setQuantity(int q) { quantity = q; }
    public String getProductName()   { return productName; }
    public double getProductPrice()  { return productPrice; }
    public String getProductImages() { return productImages; }
    public int    getProductStock()  { return productStock; }
    public double getSubtotal()      { return productPrice * quantity; }
}
