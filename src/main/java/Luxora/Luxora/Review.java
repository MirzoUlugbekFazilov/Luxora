package Luxora.Luxora;
import javafx.beans.property.*;
public class Review {
    private final IntegerProperty reviewID;
    private final IntegerProperty productID;
    private final StringProperty  productName;
    private final StringProperty  customerEmail;
    private final StringProperty  customerName;
    private final IntegerProperty rating;
    private final StringProperty  comment;
    private final StringProperty  createdAt;
    private final BooleanProperty flagged;
    private final IntegerProperty helpfulCount;
    private final IntegerProperty unhelpfulCount;
    public Review(int reviewID, int productID, String productName, String customerEmail, String customerName,
                  int rating, String comment, String createdAt, boolean flagged, int helpfulCount, int unhelpfulCount) {
        this.reviewID = new SimpleIntegerProperty(reviewID);
        this.productID = new SimpleIntegerProperty(productID);
        this.productName = new SimpleStringProperty(productName == null ? "" : productName);
        this.customerEmail = new SimpleStringProperty(customerEmail);
        this.customerName = new SimpleStringProperty(customerName == null ? "" : customerName);
        this.rating = new SimpleIntegerProperty(rating);
        this.comment = new SimpleStringProperty(comment == null ? "" : comment);
        this.createdAt = new SimpleStringProperty(createdAt == null ? "" : createdAt);
        this.flagged = new SimpleBooleanProperty(flagged);
        this.helpfulCount = new SimpleIntegerProperty(helpfulCount);
        this.unhelpfulCount = new SimpleIntegerProperty(unhelpfulCount);
    }
    public int     getReviewID()      { return reviewID.get(); }
    public int     getProductID()     { return productID.get(); }
    public String  getProductName()   { return productName.get(); }
    public String  getCustomerEmail() { return customerEmail.get(); }
    public String  getCustomerName()  { return customerName.get(); }
    public int     getRating()        { return rating.get(); }
    public String  getComment()       { return comment.get(); }
    public String  getCreatedAt()     { return createdAt.get(); }
    public boolean isFlagged()        { return flagged.get(); }
    public int     getHelpfulCount()  { return helpfulCount.get(); }
    public int     getUnhelpfulCount(){ return unhelpfulCount.get(); }
    public IntegerProperty reviewIDProperty()    { return reviewID; }
    public IntegerProperty productIDProperty()   { return productID; }
    public StringProperty  productNameProperty() { return productName; }
    public StringProperty  customerEmailProperty(){ return customerEmail; }
    public StringProperty  customerNameProperty(){ return customerName; }
    public IntegerProperty ratingProperty()      { return rating; }
    public StringProperty  commentProperty()     { return comment; }
    public StringProperty  createdAtProperty()   { return createdAt; }
    public BooleanProperty flaggedProperty()     { return flagged; }
    public IntegerProperty helpfulCountProperty(){ return helpfulCount; }
    public IntegerProperty unhelpfulCountProperty(){ return unhelpfulCount; }
    public String starsDisplay() { return "★".repeat(rating.get()) + "☆".repeat(5 - rating.get()); }
}
