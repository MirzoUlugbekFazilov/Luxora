package Luxora.Luxora;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class dbController {

    private static final String DB_URL = "jdbc:sqlite:systemDB.db";

    private static Connection connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(DB_URL);
    }

    // ── Schema initialisation ──────────────────────────────────────────────────

    public static void createUsersTable() throws Exception {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS USERS (" +
                "  id        INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name      TEXT    NOT NULL," +
                "  email     TEXT    NOT NULL UNIQUE," +
                "  password  TEXT    NOT NULL," +
                "  user_type INTEGER NOT NULL" +
                ");"
            );
        }
    }

    public static void createProductsTable() throws Exception {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS PRODUCTS (" +
                "  productID   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name        TEXT    NOT NULL," +
                "  category    TEXT    NOT NULL DEFAULT ''," +
                "  description TEXT             DEFAULT ''," +
                "  price       REAL    NOT NULL DEFAULT 0.0," +
                "  quantity    INTEGER NOT NULL DEFAULT 0," +
                "  status      TEXT    NOT NULL DEFAULT 'active'," +
                "  images      TEXT             DEFAULT ''," +
                "  createdBy   TEXT             DEFAULT ''" +
                ");"
            );
        }
    }

    /** Adds createdBy column to existing PRODUCTS tables that predate ownership tracking. */
    public static void migrateAddCreatedBy() {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE PRODUCTS ADD COLUMN createdBy TEXT DEFAULT '';");
        } catch (Exception ignored) {
            // Column already exists — safe to ignore
        }
    }

    // ── User helpers ───────────────────────────────────────────────────────────

    public static boolean emailchecker(String email) throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT email FROM USERS WHERE email = '" + email + "';")) {
            return rs.next();
        }
    }

    public static String typechecker(String email) throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT user_type FROM USERS WHERE email = '" + email + "';")) {
            if (!rs.next()) return "nothing";
            int type = rs.getInt("user_type");
            return switch (type) {
                case 1 -> "customer";
                case 2 -> "product manager";
                case 3 -> "admin";
                default -> "nothing";
            };
        }
    }

    public static boolean loginchecker(String email, String password) throws Exception {
        if (!emailchecker(email)) return false;
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT password FROM USERS WHERE email = '" + email + "';")) {
            return rs.next() && hash(password).equals(rs.getString("password"));
        }
    }

    public static void createUser(String n, String em, String pw, int type) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO USERS VALUES (NULL, ?, ?, ?, ?);")) {
            ps.setString(1, n);
            ps.setString(2, em);
            ps.setString(3, hash(pw));
            ps.setInt(4, type);
            ps.executeUpdate();
        }
    }

    /** Returns the display name of the user with the given email, or empty string. */
    public static String getUserName(String email) throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name FROM USERS WHERE email = '" + email + "';")) {
            return rs.next() ? rs.getString("name") : "";
        }
    }

    // ── Products CRUD ─────────────────────────────────────────────────────────

    /** Returns all products ordered alphabetically. */
    public static List<Product> getAllProducts() throws Exception {
        List<Product> list = new ArrayList<>();
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM PRODUCTS ORDER BY name COLLATE NOCASE;")) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        }
        return list;
    }

    /** Returns only active products (for customers). */
    public static List<Product> getActiveProducts() throws Exception {
        List<Product> list = new ArrayList<>();
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM PRODUCTS WHERE status = 'active' ORDER BY name COLLATE NOCASE;")) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        }
        return list;
    }

    /** Inserts a new product owned by createdBy (email). Returns the generated productID. */
    public static int addProduct(String name, String category, String description,
                                  double price, int quantity, String status, String images,
                                  String createdBy)
            throws Exception {
        String sql = "INSERT INTO PRODUCTS (name, category, description, price, quantity, status, images, createdBy)" +
                     " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setInt(5, quantity);
            ps.setString(6, status);
            ps.setString(7, images);
            ps.setString(8, createdBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Returns only products created by the given email (for product managers). */
    public static List<Product> getProductsByCreator(String email) throws Exception {
        List<Product> list = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM PRODUCTS WHERE createdBy=? ORDER BY name COLLATE NOCASE;")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(fromResultSet(rs));
        }
        return list;
    }

    /** Updates an existing product by productID. */
    public static void updateProduct(int id, String name, String category, String description,
                                      double price, int quantity, String status, String images)
            throws Exception {
        String sql = "UPDATE PRODUCTS SET name=?, category=?, description=?, " +
                     "price=?, quantity=?, status=?, images=? WHERE productID=?;";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setInt(5, quantity);
            ps.setString(6, status);
            ps.setString(7, images);
            ps.setInt(8, id);
            ps.executeUpdate();
        }
    }

    /** Deletes a product by productID. */
    public static void deleteProduct(int id) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM PRODUCTS WHERE productID=?;")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Toggles a product's status between 'active' and 'inactive'. */
    public static void toggleProductStatus(int productId) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE PRODUCTS SET status = CASE WHEN status='active' THEN 'inactive' ELSE 'active' END" +
                     " WHERE productID=?;")) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    /** Permanently deletes the user account with the given email. */
    public static void deleteAccount(String email) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM USERS WHERE email=?;")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    /** Deletes all products created by the given email (used when removing PM/admin accounts). */
    public static void deleteProductsByCreator(String email) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM PRODUCTS WHERE createdBy=?;")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    /** Returns all users ordered by name (no passwords returned). */
    public static List<User> getAllUsers() throws Exception {
        List<User> list = new ArrayList<>();
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name, email, user_type FROM USERS ORDER BY name COLLATE NOCASE;")) {
            while (rs.next()) {
                list.add(new User(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("user_type")));
            }
        }
        return list;
    }

    /** Updates a user's name, email, and user_type identified by their original email. */
    public static void updateUser(String originalEmail, String name, String email, int userType) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE USERS SET name=?, email=?, user_type=? WHERE email=?;")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, userType);
            ps.setString(4, originalEmail);
            ps.executeUpdate();
        }
    }

    // ── Cart CRUD ─────────────────────────────────────────────────────────────

    public static void createCartTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS CART (" +
                     "  cartID    INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "  userEmail TEXT    NOT NULL," +
                     "  productID INTEGER NOT NULL," +
                     "  quantity  INTEGER NOT NULL DEFAULT 1" +
                     ");";
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    /** Adds qty of productID to user's cart. Increments quantity if already present. */
    public static void addToCart(String email, int productID, int qty) throws Exception {
        String check = "SELECT cartID, quantity FROM CART WHERE userEmail=? AND productID=?";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(check)) {
            ps.setString(1, email);
            ps.setInt(2, productID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newQty = rs.getInt("quantity") + qty;
                try (PreparedStatement up = c.prepareStatement(
                        "UPDATE CART SET quantity=? WHERE cartID=?")) {
                    up.setInt(1, newQty);
                    up.setInt(2, rs.getInt("cartID"));
                    up.executeUpdate();
                }
            } else {
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO CART (userEmail, productID, quantity) VALUES (?,?,?)")) {
                    ins.setString(1, email);
                    ins.setInt(2, productID);
                    ins.setInt(3, qty);
                    ins.executeUpdate();
                }
            }
        }
    }

    /** Returns all cart items for the user, joined with product details. */
    public static List<CartItem> getCartItems(String email) throws Exception {
        String sql = "SELECT c.cartID, c.userEmail, c.productID, c.quantity," +
                     "       p.name, p.price, p.images, p.quantity AS stock" +
                     " FROM CART c JOIN PRODUCTS p ON c.productID = p.productID" +
                     " WHERE c.userEmail=? ORDER BY c.cartID;";
        List<CartItem> items = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new CartItem(
                        rs.getInt("cartID"),
                        rs.getString("userEmail"),
                        rs.getInt("productID"),
                        rs.getInt("quantity"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("images"),
                        rs.getInt("stock")));
            }
        }
        return items;
    }

    public static void updateCartQuantity(int cartID, int quantity) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE CART SET quantity=? WHERE cartID=?")) {
            ps.setInt(1, quantity);
            ps.setInt(2, cartID);
            ps.executeUpdate();
        }
    }

    public static void removeFromCart(int cartID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM CART WHERE cartID=?")) {
            ps.setInt(1, cartID);
            ps.executeUpdate();
        }
    }

    /** Removes all cart items for the given user. */
    public static void clearCart(String email) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM CART WHERE userEmail=?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    /** Returns total quantity of all items in the user's cart. */
    public static int getCartItemCount(String email) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(quantity),0) FROM CART WHERE userEmail=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── Password hashing ───────────────────────────────────────────────────────

    private static String hash(String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(pw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private static Product fromResultSet(ResultSet rs) throws Exception {
        return new Product(
                rs.getInt("productID"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getString("status"),
                rs.getString("images"),
                rs.getString("createdBy")
        );
    }

    // ── Default seed data ──────────────────────────────────────────────────────

    /** Creates default accounts if they don't already exist. Safe to call every startup. */
    public static void seedDefaultUsers() throws Exception {
        if (!emailchecker("realadmin@gmail.com"))
            createUser("admin", "realadmin@gmail.com", "admin2007", 3);
    }

    // ── Orders tables ─────────────────────────────────────────────────────────

    public static void createOrdersTable() throws Exception {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ORDERS (" +
                "  orderID   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  orderCode TEXT    NOT NULL UNIQUE," +
                "  userEmail TEXT    NOT NULL," +
                "  total     REAL    NOT NULL DEFAULT 0.0," +
                "  status    TEXT    NOT NULL DEFAULT 'Processing'," +
                "  orderDate TEXT    NOT NULL" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ORDER_ITEMS (" +
                "  itemID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  orderID     INTEGER NOT NULL," +
                "  productID   INTEGER NOT NULL," +
                "  productName TEXT    NOT NULL," +
                "  quantity    INTEGER NOT NULL," +
                "  price       REAL    NOT NULL" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ORDER_RETURNS (" +
                "  returnID   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  orderID    INTEGER NOT NULL," +
                "  reason     TEXT    NOT NULL," +
                "  returnDate TEXT    NOT NULL" +
                ");"
            );
        }
    }

    // ── Orders CRUD ──────────────────────────────────────────────────────────

    /**
     * Places a new order using a transaction: inserts into ORDERS then ORDER_ITEMS,
     * and clears the user's cart on success. Atomicity is guaranteed — either all
     * records are saved or none are (rollback on failure).
     */
    public static void placeOrder(String userEmail, List<CartItem> cartItems, double total)
            throws Exception {
        Connection c = connect();
        try {
            c.setAutoCommit(false);

            // 1. Stock verification — reject if any item has insufficient stock
            for (CartItem item : cartItems) {
                int stock = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT quantity FROM PRODUCTS WHERE productID=? AND status='active';")) {
                    ps.setInt(1, item.getProductID());
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) throw new Exception(
                            "Product '" + item.getProductName() + "' is no longer available.");
                    stock = rs.getInt("quantity");
                }
                if (stock < item.getQuantity()) throw new Exception(
                        "Insufficient stock for '" + item.getProductName() +
                        "'. Available: " + stock + ", requested: " + item.getQuantity());
            }

            // 2. Generate unique order code
            int count;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ORDERS")) {
                count = rs.next() ? rs.getInt(1) + 1 : 1;
            }
            String orderCode = "LUX-" + String.format("%04d", count);
            String today = java.time.LocalDate.now().toString();

            // 3. Insert order header
            int orderId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ORDERS (orderCode, userEmail, total, status, orderDate)" +
                    " VALUES (?, ?, ?, 'Processing', ?);",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, orderCode);
                ps.setString(2, userEmail);
                ps.setDouble(3, total);
                ps.setString(4, today);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    orderId = keys.getInt(1);
                }
            }

            // 4. Insert order items
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ORDER_ITEMS (orderID, productID, productName, quantity, price)" +
                    " VALUES (?, ?, ?, ?, ?);")) {
                for (CartItem item : cartItems) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProductID());
                    ps.setString(3, item.getProductName());
                    ps.setInt(4, item.getQuantity());
                    ps.setDouble(5, item.getProductPrice());
                    ps.executeUpdate();
                }
            }

            // 5. Decrement stock for each ordered product
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE PRODUCTS SET quantity = MAX(0, quantity - ?) WHERE productID=?;")) {
                for (CartItem item : cartItems) {
                    ps.setInt(1, item.getQuantity());
                    ps.setInt(2, item.getProductID());
                    ps.executeUpdate();
                }
            }

            c.commit();

            // 6. Clear cart after successful order
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM CART WHERE userEmail=?;")) {
                ps.setString(1, userEmail);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ignored) {}
            throw e;
        } finally {
            c.setAutoCommit(true);
            c.close();
        }
    }

    /** Returns all orders for the given user email, newest first. */
    public static List<Order> getOrdersByEmail(String email) throws Exception {
        List<Order> list = new ArrayList<>();
        String sql =
            "SELECT o.orderID, o.orderCode, o.userEmail, o.total, o.status, o.orderDate," +
            "       (SELECT COUNT(*) FROM ORDER_ITEMS i WHERE i.orderID = o.orderID) AS itemCount" +
            " FROM ORDERS o WHERE o.userEmail = ? ORDER BY o.orderID DESC;";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("orderID"),
                    rs.getString("orderCode"),
                    rs.getString("userEmail"),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    rs.getString("orderDate"),
                    rs.getInt("itemCount")
                ));
            }
        }
        return list;
    }

    /**
     * Returns orders that contain at least one product created by the given manager email.
     * Used by product managers to see orders that include their products.
     */
    public static List<Order> getOrdersByProductManager(String managerEmail) throws Exception {
        List<Order> list = new ArrayList<>();
        String sql =
            "SELECT DISTINCT o.orderID, o.orderCode, o.userEmail, o.total, o.status, o.orderDate," +
            "       (SELECT COUNT(*) FROM ORDER_ITEMS i WHERE i.orderID = o.orderID) AS itemCount" +
            " FROM ORDERS o" +
            " JOIN ORDER_ITEMS oi ON oi.orderID = o.orderID" +
            " JOIN PRODUCTS p ON p.productID = oi.productID AND p.createdBy = ?" +
            " ORDER BY o.orderID DESC;";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, managerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("orderID"),
                    rs.getString("orderCode"),
                    rs.getString("userEmail"),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    rs.getString("orderDate"),
                    rs.getInt("itemCount")
                ));
            }
        }
        return list;
    }

    /** Returns ALL orders across all users (for admin view), newest first. */
    public static List<Order> getAllOrders() throws Exception {
        List<Order> list = new ArrayList<>();
        String sql =
            "SELECT o.orderID, o.orderCode, o.userEmail, o.total, o.status, o.orderDate," +
            "       (SELECT COUNT(*) FROM ORDER_ITEMS i WHERE i.orderID = o.orderID) AS itemCount" +
            " FROM ORDERS o ORDER BY o.orderID DESC;";
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("orderID"),
                    rs.getString("orderCode"),
                    rs.getString("userEmail"),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    rs.getString("orderDate"),
                    rs.getInt("itemCount")
                ));
            }
        }
        return list;
    }

    /**
     * Updates the status of an order.
     * When status transitions to "Shipped", automatically creates a DELIVERIES record
     * (unless one already exists for this order), so logistics panels stay in sync.
     */
    public static void updateOrderStatus(int orderID, String newStatus) throws Exception {
        // Fetch order metadata before updating
        String orderCode = null;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT orderCode FROM ORDERS WHERE orderID=?;")) {
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) orderCode = rs.getString("orderCode");
        }

        // Perform the status update
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE ORDERS SET status=? WHERE orderID=?;")) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderID);
            ps.executeUpdate();
        }

        // Auto-create delivery record when order is Shipped
        if ("Shipped".equals(newStatus) && orderCode != null) {
            boolean exists;
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT COUNT(*) FROM DELIVERIES WHERE orderRef=?;")) {
                ps.setString(1, orderCode);
                ResultSet rs = ps.executeQuery();
                exists = rs.next() && rs.getInt(1) > 0;
            }
            if (!exists) {
                // Auto-assign first available driver (if any)
                String autoDriver = null;
                try (Connection c = connect();
                     Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT driverId FROM DRIVERS WHERE isActive=1 ORDER BY driverId LIMIT 1;")) {
                    if (rs.next()) autoDriver = rs.getString("driverId");
                }
                createDelivery(orderCode, "Pending shipment — " + orderCode, autoDriver);
            }
        }
    }

    // ── Delivery tables ───────────────────────────────────────────────────────

    public static void createDeliveryTables() throws Exception {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS DELIVERIES (" +
                "  deliveryId      TEXT PRIMARY KEY," +
                "  orderRef        TEXT NOT NULL," +
                "  deliveryAddress TEXT NOT NULL," +
                "  assignedDriver  TEXT," +
                "  status          TEXT NOT NULL DEFAULT 'Pending'," +
                "  createdTime     TEXT NOT NULL" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS DRIVERS (" +
                "  driverId TEXT PRIMARY KEY," +
                "  fullName TEXT NOT NULL," +
                "  isActive INTEGER NOT NULL DEFAULT 1" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS DELIVERY_STATUS_LOG (" +
                "  logID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  deliveryId TEXT NOT NULL," +
                "  newStatus  TEXT NOT NULL," +
                "  loggedAt   TEXT NOT NULL" +
                ");"
            );
        }
    }

    /** Seeds default drivers if none exist. Safe to call every startup. */
    public static void seedDefaultDrivers() throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM DRIVERS")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String sql = "INSERT OR IGNORE INTO DRIVERS (driverId, fullName, isActive) VALUES (?,?,1);";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String[][] drivers = {
                {"DRV-001", "Levon Mercer"},
                {"DRV-002", "Nadia Voss"},
                {"DRV-003", "Dorian Pike"}
            };
            for (String[] d : drivers) {
                ps.setString(1, d[0]);
                ps.setString(2, d[1]);
                ps.executeUpdate();
            }
        }
    }

    // ── Delivery CRUD ─────────────────────────────────────────────────────────

    /** Returns all deliveries ordered by creation time descending. */
    public static List<Delivery> getAllDeliveries() throws Exception {
        List<Delivery> list = new ArrayList<>();
        String sql =
            "SELECT d.deliveryId, d.orderRef, d.deliveryAddress," +
            "       COALESCE(dr.fullName, d.assignedDriver, '') AS driverName," +
            "       d.status, d.createdTime" +
            " FROM DELIVERIES d" +
            " LEFT JOIN DRIVERS dr ON dr.driverId = d.assignedDriver" +
            " ORDER BY d.createdTime DESC;";
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Delivery(
                    rs.getString("deliveryId"),
                    rs.getString("orderRef"),
                    rs.getString("deliveryAddress"),
                    rs.getString("driverName"),
                    rs.getString("status"),
                    rs.getString("createdTime")
                ));
            }
        }
        return list;
    }

    /** Returns list of active drivers as "driverId|fullName" pairs. */
    public static List<String> getAvailableDrivers() throws Exception {
        List<String> list = new ArrayList<>();
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT driverId, fullName FROM DRIVERS WHERE isActive=1 ORDER BY fullName;")) {
            while (rs.next()) {
                list.add(rs.getString("driverId") + "|" + rs.getString("fullName"));
            }
        }
        return list;
    }

    /** Creates a new delivery record with status 'Pending'. */
    public static void createDelivery(String orderRef, String deliveryAddress,
                                       String driverId) throws Exception {
        String deliveryId = "DEL-" + System.currentTimeMillis();
        String now = java.time.LocalDateTime.now().toString();
        String sql = "INSERT INTO DELIVERIES (deliveryId, orderRef, deliveryAddress," +
                     " assignedDriver, status, createdTime) VALUES (?,?,?,?,'Pending',?);";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, deliveryId);
            ps.setString(2, orderRef);
            ps.setString(3, deliveryAddress);
            ps.setString(4, (driverId == null || driverId.isBlank()) ? null : driverId);
            ps.setString(5, now);
            ps.executeUpdate();
        }
    }

    /** Assigns a driver to a delivery (must be in Pending status). */
    public static void assignDeliveryDriver(String deliveryId, String driverId) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE DELIVERIES SET assignedDriver=? WHERE deliveryId=?;")) {
            ps.setString(1, driverId);
            ps.setString(2, deliveryId);
            ps.executeUpdate();
        }
    }

    /**
     * Advances a delivery's status by one step:
     * Pending → In Transit → Out for Delivery → Delivered.
     * Enforces: only one active (In Transit/Out for Delivery) delivery per driver.
     * Logs each transition. When Delivered, marks the linked order as Delivered too.
     * Returns false if the delivery is already in a terminal state.
     */
    public static boolean advanceDeliveryStatus(String deliveryId) throws Exception {
        String current;
        String assignedDriver;
        String orderRef;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT status, assignedDriver, orderRef FROM DELIVERIES WHERE deliveryId=?;")) {
            ps.setString(1, deliveryId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            current        = rs.getString("status");
            assignedDriver = rs.getString("assignedDriver");
            orderRef       = rs.getString("orderRef");
        }
        String next = switch (current) {
            case "Pending"          -> "In Transit";
            case "In Transit"       -> "Out for Delivery";
            case "Out for Delivery" -> "Delivered";
            default                 -> null;
        };
        if (next == null) return false;

        // Enforce one active delivery per driver when starting transit
        if ("In Transit".equals(next) && assignedDriver != null && !assignedDriver.isBlank()) {
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT COUNT(*) FROM DELIVERIES" +
                         " WHERE assignedDriver=? AND status IN ('In Transit','Out for Delivery')" +
                         " AND deliveryId!=?;")) {
                ps.setString(1, assignedDriver);
                ps.setString(2, deliveryId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0)
                    throw new Exception(
                            "Driver already has an active delivery in progress. " +
                            "Complete it before starting another.");
            }
        }

        // Update status
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE DELIVERIES SET status=? WHERE deliveryId=?;")) {
            ps.setString(1, next);
            ps.setString(2, deliveryId);
            ps.executeUpdate();
        }

        // Log the transition
        logDeliveryTransition(deliveryId, next);

        // When delivered, propagate status to the linked ORDERS row
        if ("Delivered".equals(next) && orderRef != null) {
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE ORDERS SET status='Delivered' WHERE orderCode=?;")) {
                ps.setString(1, orderRef);
                ps.executeUpdate();
            }
        }
        return true;
    }

    /** Marks a delivery as Failed, logs the transition, and propagates 'Failed' to the linked order. */
    public static void failDelivery(String deliveryId) throws Exception {
        // Look up orderRef before updating
        String orderRef = null;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT orderRef FROM DELIVERIES WHERE deliveryId=?;")) {
            ps.setString(1, deliveryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) orderRef = rs.getString("orderRef");
        }
        // Mark delivery as Failed
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE DELIVERIES SET status='Failed' WHERE deliveryId=?;")) {
            ps.setString(1, deliveryId);
            ps.executeUpdate();
        }
        logDeliveryTransition(deliveryId, "Failed");
        // Propagate Failed status to the linked order
        if (orderRef != null) {
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE ORDERS SET status='Failed' WHERE orderCode=?;")) {
                ps.setString(1, orderRef);
                ps.executeUpdate();
            }
        }
    }

    /** Logs a delivery status transition with a UTC timestamp. */
    public static void logDeliveryTransition(String deliveryId, String newStatus) {
        try {
            String now = java.time.LocalDateTime.now().toString();
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO DELIVERY_STATUS_LOG (deliveryId, newStatus, loggedAt)" +
                         " VALUES (?,?,?);")) {
                ps.setString(1, deliveryId);
                ps.setString(2, newStatus);
                ps.setString(3, now);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    /** Returns the line items for an order as [productName, quantity, formattedPrice] arrays. */
    public static List<String[]> getOrderItems(int orderID) throws Exception {
        List<String[]> items = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT productName, quantity, price FROM ORDER_ITEMS" +
                     " WHERE orderID=? ORDER BY itemID;")) {
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new String[]{
                    rs.getString("productName"),
                    String.valueOf(rs.getInt("quantity")),
                    String.format("$%.2f", rs.getDouble("price"))
                });
            }
        }
        return items;
    }

    /**
     * Submits a return request for the given order.
     * Inserts a row into ORDER_RETURNS and updates ORDERS status to 'Returned'.
     * Atomic — rolled back on failure.
     */
    public static void submitReturnRequest(int orderID, String reason) throws Exception {
        String today = java.time.LocalDate.now().toString();
        Connection c = connect();
        try {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ORDER_RETURNS (orderID, reason, returnDate) VALUES (?,?,?);")) {
                ps.setInt(1, orderID);
                ps.setString(2, reason);
                ps.setString(3, today);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE ORDERS SET status='Returned' WHERE orderID=?;")) {
                ps.setInt(1, orderID);
                ps.executeUpdate();
            }
            c.commit();
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ignored) {}
            throw e;
        } finally {
            c.setAutoCommit(true);
            c.close();
        }
    }

    // ── Inventory / Warehousing tables ────────────────────────────────────────

    public static void createInventoryTables() throws Exception {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS WAREHOUSES (" +
                "  warehouseID  INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name         TEXT    NOT NULL," +
                "  location     TEXT    NOT NULL DEFAULT ''," +
                "  contactInfo  TEXT             DEFAULT ''," +
                "  capacity     INTEGER          DEFAULT -1" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS WAREHOUSE_STOCK (" +
                "  stockID         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  warehouseID     INTEGER NOT NULL REFERENCES WAREHOUSES(warehouseID)," +
                "  productID       INTEGER NOT NULL REFERENCES PRODUCTS(productID)," +
                "  quantity        INTEGER NOT NULL DEFAULT 0," +
                "  minThreshold    INTEGER NOT NULL DEFAULT 0," +
                "  lastRestockDate TEXT             DEFAULT ''," +
                "  reorderQty      INTEGER          DEFAULT 0," +
                "  UNIQUE(warehouseID, productID)" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS STOCK_HISTORY (" +
                "  historyID   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  warehouseID INTEGER NOT NULL," +
                "  productID   INTEGER NOT NULL," +
                "  changeQty   INTEGER NOT NULL," +
                "  reason      TEXT    NOT NULL DEFAULT ''," +
                "  changedAt   TEXT    NOT NULL" +
                ");"
            );
        }
    }

    public static List<Warehouse> getAllWarehouses() throws Exception {
        List<Warehouse> list = new ArrayList<>();
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM WAREHOUSES ORDER BY name;")) {
            while (rs.next()) {
                list.add(new Warehouse(
                    rs.getInt("warehouseID"), rs.getString("name"),
                    rs.getString("location"), rs.getString("contactInfo"),
                    rs.getInt("capacity")));
            }
        }
        return list;
    }

    public static void createWarehouse(String name, String location, String contactInfo, int capacity) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Warehouse name cannot be empty.");
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO WAREHOUSES (name, location, contactInfo, capacity) VALUES (?,?,?,?);")) {
            ps.setString(1, name.trim());
            ps.setString(2, location == null ? "" : location.trim());
            ps.setString(3, contactInfo == null ? "" : contactInfo.trim());
            ps.setInt(4, capacity);
            ps.executeUpdate();
        }
    }

    public static void updateWarehouse(int warehouseID, String name, String location, String contactInfo, int capacity) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Warehouse name cannot be empty.");
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE WAREHOUSES SET name=?, location=?, contactInfo=?, capacity=? WHERE warehouseID=?;")) {
            ps.setString(1, name.trim());
            ps.setString(2, location == null ? "" : location.trim());
            ps.setString(3, contactInfo == null ? "" : contactInfo.trim());
            ps.setInt(4, capacity);
            ps.setInt(5, warehouseID);
            ps.executeUpdate();
        }
    }

    public static void deleteWarehouse(int warehouseID) throws Exception {
        // Check for stock first
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM WAREHOUSE_STOCK WHERE warehouseID=?;")) {
            ps.setInt(1, warehouseID);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new Exception("Cannot delete warehouse with existing stock records. Remove all stock first.");
        }
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM WAREHOUSES WHERE warehouseID=?;")) {
            ps.setInt(1, warehouseID);
            ps.executeUpdate();
        }
    }

    public static List<WarehouseStock> getAllStock() throws Exception {
        List<WarehouseStock> list = new ArrayList<>();
        String sql =
            "SELECT ws.stockID, ws.warehouseID, w.name AS warehouseName," +
            "       ws.productID, p.name AS productName, p.category," +
            "       ws.quantity, ws.minThreshold, ws.lastRestockDate, ws.reorderQty" +
            " FROM WAREHOUSE_STOCK ws" +
            " JOIN WAREHOUSES w ON w.warehouseID = ws.warehouseID" +
            " JOIN PRODUCTS p ON p.productID = ws.productID" +
            " ORDER BY w.name, p.name;";
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new WarehouseStock(
                    rs.getInt("stockID"), rs.getInt("warehouseID"), rs.getString("warehouseName"),
                    rs.getInt("productID"), rs.getString("productName"), rs.getString("category"),
                    rs.getInt("quantity"), rs.getInt("minThreshold"),
                    rs.getString("lastRestockDate"), rs.getInt("reorderQty")));
            }
        }
        return list;
    }

    public static List<WarehouseStock> getLowStock() throws Exception {
        List<WarehouseStock> list = new ArrayList<>();
        String sql =
            "SELECT ws.stockID, ws.warehouseID, w.name AS warehouseName," +
            "       ws.productID, p.name AS productName, p.category," +
            "       ws.quantity, ws.minThreshold, ws.lastRestockDate, ws.reorderQty" +
            " FROM WAREHOUSE_STOCK ws" +
            " JOIN WAREHOUSES w ON w.warehouseID = ws.warehouseID" +
            " JOIN PRODUCTS p ON p.productID = ws.productID" +
            " WHERE ws.quantity < ws.minThreshold" +
            " ORDER BY (ws.minThreshold - ws.quantity) DESC;";
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new WarehouseStock(
                    rs.getInt("stockID"), rs.getInt("warehouseID"), rs.getString("warehouseName"),
                    rs.getInt("productID"), rs.getString("productName"), rs.getString("category"),
                    rs.getInt("quantity"), rs.getInt("minThreshold"),
                    rs.getString("lastRestockDate"), rs.getInt("reorderQty")));
            }
        }
        return list;
    }

    public static void assignStockToWarehouse(int warehouseID, int productID, int quantity, int minThreshold, int reorderQty) throws Exception {
        if (quantity < 0) throw new Exception("Quantity cannot be negative.");
        if (minThreshold < 0) throw new Exception("Minimum threshold cannot be negative.");
        // Check warehouse capacity
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT capacity FROM WAREHOUSES WHERE warehouseID=?;")) {
            ps.setInt(1, warehouseID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cap = rs.getInt("capacity");
                if (cap >= 0) {
                    int currentTotal = 0;
                    try (PreparedStatement ps2 = c.prepareStatement(
                            "SELECT COALESCE(SUM(quantity),0) FROM WAREHOUSE_STOCK WHERE warehouseID=?;")) {
                        ps2.setInt(1, warehouseID);
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) currentTotal = rs2.getInt(1);
                    }
                    if (currentTotal + quantity > cap)
                        throw new Exception("Adding " + quantity + " units would exceed warehouse capacity of " + cap + ".");
                }
            }
        }
        String today = java.time.LocalDate.now().toString();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO WAREHOUSE_STOCK (warehouseID, productID, quantity, minThreshold, lastRestockDate, reorderQty)" +
                     " VALUES (?,?,?,?,?,?);")) {
            ps.setInt(1, warehouseID);
            ps.setInt(2, productID);
            ps.setInt(3, quantity);
            ps.setInt(4, minThreshold);
            ps.setString(5, quantity > 0 ? today : "");
            ps.setInt(6, reorderQty);
            ps.executeUpdate();
        }
        if (quantity > 0) logStockChange(warehouseID, productID, quantity, "Initial stock assignment");
    }

    public static void restockWarehouseProduct(int stockID, int addQty) throws Exception {
        if (addQty <= 0) throw new Exception("Restock quantity must be positive.");
        // Get current info
        int warehouseID = 0; int productID = 0; int currentQty = 0; int cap = -1;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT ws.warehouseID, ws.productID, ws.quantity, w.capacity" +
                     " FROM WAREHOUSE_STOCK ws JOIN WAREHOUSES w ON w.warehouseID=ws.warehouseID" +
                     " WHERE ws.stockID=?;")) {
            ps.setInt(1, stockID);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new Exception("Stock record not found.");
            warehouseID = rs.getInt("warehouseID");
            productID   = rs.getInt("productID");
            currentQty  = rs.getInt("quantity");
            cap         = rs.getInt("capacity");
        }
        if (cap >= 0) {
            int currentTotal = 0;
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT COALESCE(SUM(quantity),0) FROM WAREHOUSE_STOCK WHERE warehouseID=?;")) {
                ps.setInt(1, warehouseID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) currentTotal = rs.getInt(1);
            }
            if (currentTotal + addQty > cap)
                throw new Exception("Restocking would exceed warehouse capacity of " + cap + ".");
        }
        String today = java.time.LocalDate.now().toString();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE WAREHOUSE_STOCK SET quantity=quantity+?, lastRestockDate=? WHERE stockID=?;")) {
            ps.setInt(1, addQty);
            ps.setString(2, today);
            ps.setInt(3, stockID);
            ps.executeUpdate();
        }
        logStockChange(warehouseID, productID, addQty, "Restock");
    }

    public static void transferStock(int fromStockID, int toWarehouseID, int qty) throws Exception {
        if (qty <= 0) throw new Exception("Transfer quantity must be positive.");
        int fromWarehouseID = 0; int productID = 0; int fromQty = 0;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT warehouseID, productID, quantity FROM WAREHOUSE_STOCK WHERE stockID=?;")) {
            ps.setInt(1, fromStockID);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new Exception("Source stock record not found.");
            fromWarehouseID = rs.getInt("warehouseID");
            productID       = rs.getInt("productID");
            fromQty         = rs.getInt("quantity");
        }
        if (fromWarehouseID == toWarehouseID) throw new Exception("Source and destination warehouse must be different.");
        if (fromQty < qty) throw new Exception("Insufficient stock. Available: " + fromQty + ", requested: " + qty + ".");
        // Check destination capacity
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT capacity FROM WAREHOUSES WHERE warehouseID=?;")) {
            ps.setInt(1, toWarehouseID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cap = rs.getInt("capacity");
                if (cap >= 0) {
                    int currentTotal = 0;
                    try (PreparedStatement ps2 = c.prepareStatement(
                            "SELECT COALESCE(SUM(quantity),0) FROM WAREHOUSE_STOCK WHERE warehouseID=?;")) {
                        ps2.setInt(1, toWarehouseID);
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) currentTotal = rs2.getInt(1);
                    }
                    if (currentTotal + qty > cap)
                        throw new Exception("Transfer would exceed destination warehouse capacity of " + cap + ".");
                }
            }
        }
        Connection c = connect();
        try {
            c.setAutoCommit(false);
            // Deduct from source
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE WAREHOUSE_STOCK SET quantity=quantity-? WHERE stockID=?;")) {
                ps.setInt(1, qty); ps.setInt(2, fromStockID); ps.executeUpdate();
            }
            // Add to destination (insert if not exists, else update)
            String today = java.time.LocalDate.now().toString();
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO WAREHOUSE_STOCK (warehouseID, productID, quantity, minThreshold, lastRestockDate, reorderQty)" +
                    " VALUES (?,?,?,0,?,0)" +
                    " ON CONFLICT(warehouseID, productID) DO UPDATE SET quantity=quantity+excluded.quantity, lastRestockDate=excluded.lastRestockDate;")) {
                ps.setInt(1, toWarehouseID); ps.setInt(2, productID); ps.setInt(3, qty); ps.setString(4, today);
                ps.executeUpdate();
            }
            c.commit();
        } catch (Exception e) {
            try { c.rollback(); } catch (Exception ignored) {}
            throw e;
        } finally {
            c.setAutoCommit(true); c.close();
        }
        logStockChange(fromWarehouseID, productID, -qty, "Transfer out to warehouse #" + toWarehouseID);
        logStockChange(toWarehouseID, productID, qty, "Transfer in from warehouse #" + fromWarehouseID);
    }

    public static void removeStockRecord(int stockID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM WAREHOUSE_STOCK WHERE stockID=?;")) {
            ps.setInt(1, stockID); ps.executeUpdate();
        }
    }

    private static void logStockChange(int warehouseID, int productID, int changeQty, String reason) {
        try {
            String now = java.time.LocalDateTime.now().toString();
            try (Connection c = connect();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO STOCK_HISTORY (warehouseID, productID, changeQty, reason, changedAt) VALUES (?,?,?,?,?);")) {
                ps.setInt(1, warehouseID); ps.setInt(2, productID); ps.setInt(3, changeQty);
                ps.setString(4, reason); ps.setString(5, now);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    public static List<String[]> getStockHistory(int warehouseID, int productID) throws Exception {
        List<String[]> list = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT changeQty, reason, changedAt FROM STOCK_HISTORY" +
                     " WHERE warehouseID=? AND productID=? ORDER BY historyID DESC LIMIT 50;")) {
            ps.setInt(1, warehouseID); ps.setInt(2, productID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("changedAt").substring(0, Math.min(16, rs.getString("changedAt").length())),
                    (rs.getInt("changeQty") > 0 ? "+" : "") + rs.getInt("changeQty"),
                    rs.getString("reason")
                });
            }
        }
        return list;
    }

    public static List<String[]> getTotalStockReport() throws Exception {
        List<String[]> list = new ArrayList<>();
        String sql =
            "SELECT p.productID, p.name, p.category," +
            "       COALESCE(SUM(ws.quantity),0) AS totalQty," +
            "       COUNT(ws.warehouseID) AS warehouseCount," +
            "       p.quantity AS catalogueQty" +
            " FROM PRODUCTS p" +
            " LEFT JOIN WAREHOUSE_STOCK ws ON ws.productID = p.productID" +
            " GROUP BY p.productID ORDER BY p.name;";
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("productID")),
                    rs.getString("name"),
                    rs.getString("category"),
                    String.valueOf(rs.getInt("totalQty")),
                    String.valueOf(rs.getInt("warehouseCount")),
                    String.valueOf(rs.getInt("catalogueQty"))
                });
            }
        }
        return list;
    }

    public static List<String[]> getWarehouseCapacityReport() throws Exception {
        List<String[]> list = new ArrayList<>();
        String sql =
            "SELECT w.warehouseID, w.name, w.location, w.capacity," +
            "       COALESCE(SUM(ws.quantity),0) AS usedQty" +
            " FROM WAREHOUSES w" +
            " LEFT JOIN WAREHOUSE_STOCK ws ON ws.warehouseID = w.warehouseID" +
            " GROUP BY w.warehouseID ORDER BY w.name;";
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int cap = rs.getInt("capacity");
                int used = rs.getInt("usedQty");
                String utilization = cap < 0 ? "N/A" : String.format("%.1f%%", (used * 100.0 / cap));
                list.add(new String[]{
                    rs.getString("name"), rs.getString("location"),
                    cap < 0 ? "Unlimited" : String.valueOf(cap),
                    String.valueOf(used), utilization
                });
            }
        }
        return list;
    }

    // ── Reviews & Ratings tables ───────────────────────────────────────────────

    public static void createReviewsTables() throws Exception {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS REVIEWS (" +
                "  reviewID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  productID     INTEGER NOT NULL," +
                "  customerEmail TEXT    NOT NULL," +
                "  rating        INTEGER NOT NULL," +
                "  comment       TEXT    NOT NULL DEFAULT ''," +
                "  createdAt     TEXT    NOT NULL," +
                "  flagged       INTEGER NOT NULL DEFAULT 0" +
                ");"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS REVIEW_HELPFUL (" +
                "  helpID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  reviewID    INTEGER NOT NULL," +
                "  voterEmail  TEXT    NOT NULL," +
                "  helpful     INTEGER NOT NULL DEFAULT 1," +
                "  UNIQUE(reviewID, voterEmail)" +
                ");"
            );
        }
    }

    public static boolean hasCustomerPurchasedProduct(String email, int productID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM ORDERS o" +
                     " JOIN ORDER_ITEMS oi ON oi.orderID = o.orderID" +
                     " WHERE o.userEmail=? AND oi.productID=? AND o.status IN ('Delivered');")) {
            ps.setString(1, email); ps.setInt(2, productID);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static boolean hasCustomerReviewedProduct(String email, int productID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM REVIEWS WHERE customerEmail=? AND productID=?;")) {
            ps.setString(1, email); ps.setInt(2, productID);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static void submitReview(int productID, String customerEmail, int rating, String comment) throws Exception {
        if (rating < 1 || rating > 5) throw new Exception("Rating must be between 1 and 5.");
        if (comment == null) comment = "";
        // Validate for disallowed special characters
        if (comment.matches(".*[@#/\\\\?\"'~`$].*"))
            throw new Exception("Comment contains disallowed characters: @ # / \\ ? \" ' ~ ` $");
        if (!hasCustomerPurchasedProduct(customerEmail, productID))
            throw new Exception("You can only review products you have purchased and received.");
        if (hasCustomerReviewedProduct(customerEmail, productID))
            throw new Exception("You have already reviewed this product.");
        String now = java.time.LocalDateTime.now().toString();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO REVIEWS (productID, customerEmail, rating, comment, createdAt, flagged) VALUES (?,?,?,?,?,0);")) {
            ps.setInt(1, productID); ps.setString(2, customerEmail);
            ps.setInt(3, rating); ps.setString(4, comment.trim()); ps.setString(5, now);
            ps.executeUpdate();
        }
    }

    public static void updateReview(int reviewID, String customerEmail, int rating, String comment) throws Exception {
        if (rating < 1 || rating > 5) throw new Exception("Rating must be between 1 and 5.");
        if (comment == null) comment = "";
        if (comment.matches(".*[@#/\\\\?\"'~`$].*"))
            throw new Exception("Comment contains disallowed characters: @ # / \\ ? \" ' ~ ` $");
        // Check ownership and time window
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT customerEmail, createdAt FROM REVIEWS WHERE reviewID=?;")) {
            ps.setInt(1, reviewID);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new Exception("Review not found.");
            if (!rs.getString("customerEmail").equals(customerEmail))
                throw new Exception("You can only edit your own reviews.");
            java.time.LocalDateTime created = java.time.LocalDateTime.parse(rs.getString("createdAt"));
            long minutesElapsed = java.time.Duration.between(created, java.time.LocalDateTime.now()).toMinutes();
            if (minutesElapsed > 5)
                throw new Exception("Reviews can only be edited within 5 minutes of posting.");
        }
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE REVIEWS SET rating=?, comment=? WHERE reviewID=? AND customerEmail=?;")) {
            ps.setInt(1, rating); ps.setString(2, comment.trim());
            ps.setInt(3, reviewID); ps.setString(4, customerEmail);
            ps.executeUpdate();
        }
    }

    public static void deleteReview(int reviewID, String customerEmail) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT customerEmail, createdAt FROM REVIEWS WHERE reviewID=?;")) {
            ps.setInt(1, reviewID);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new Exception("Review not found.");
            if (!rs.getString("customerEmail").equals(customerEmail))
                throw new Exception("You can only delete your own reviews.");
            java.time.LocalDateTime created = java.time.LocalDateTime.parse(rs.getString("createdAt"));
            long minutesElapsed = java.time.Duration.between(created, java.time.LocalDateTime.now()).toMinutes();
            if (minutesElapsed > 5)
                throw new Exception("Reviews can only be deleted within 5 minutes of posting.");
        }
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM REVIEWS WHERE reviewID=?;")) {
            ps.setInt(1, reviewID); ps.executeUpdate();
        }
    }

    public static void adminDeleteReview(int reviewID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM REVIEWS WHERE reviewID=?;")) {
            ps.setInt(1, reviewID); ps.executeUpdate();
        }
    }

    public static void flagReview(int reviewID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("UPDATE REVIEWS SET flagged=1 WHERE reviewID=?;")) {
            ps.setInt(1, reviewID); ps.executeUpdate();
        }
    }

    public static void unflagReview(int reviewID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("UPDATE REVIEWS SET flagged=0 WHERE reviewID=?;")) {
            ps.setInt(1, reviewID); ps.executeUpdate();
        }
    }

    public static void voteHelpful(int reviewID, String voterEmail, boolean helpful) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO REVIEW_HELPFUL (reviewID, voterEmail, helpful) VALUES (?,?,?)" +
                     " ON CONFLICT(reviewID, voterEmail) DO UPDATE SET helpful=excluded.helpful;")) {
            ps.setInt(1, reviewID); ps.setString(2, voterEmail); ps.setInt(3, helpful ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public static List<Review> getReviewsForProduct(int productID) throws Exception {
        List<Review> list = new ArrayList<>();
        String sql =
            "SELECT r.reviewID, r.productID, p.name AS productName, r.customerEmail," +
            "       COALESCE(u.name, r.customerEmail) AS customerName," +
            "       r.rating, r.comment, r.createdAt, r.flagged," +
            "       COALESCE(h.helpfulCount, 0) AS helpfulCount," +
            "       COALESCE(u2.unhelpfulCount, 0) AS unhelpfulCount" +
            " FROM REVIEWS r" +
            " JOIN PRODUCTS p ON p.productID = r.productID" +
            " LEFT JOIN USERS u ON u.email = r.customerEmail" +
            " LEFT JOIN (SELECT reviewID, COUNT(*) AS helpfulCount FROM REVIEW_HELPFUL WHERE helpful=1 GROUP BY reviewID) h ON h.reviewID=r.reviewID" +
            " LEFT JOIN (SELECT reviewID, COUNT(*) AS unhelpfulCount FROM REVIEW_HELPFUL WHERE helpful=0 GROUP BY reviewID) u2 ON u2.reviewID=r.reviewID" +
            " WHERE r.productID=? ORDER BY r.createdAt DESC;";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Review(rs.getInt("reviewID"), rs.getInt("productID"), rs.getString("productName"),
                    rs.getString("customerEmail"), rs.getString("customerName"),
                    rs.getInt("rating"), rs.getString("comment"), rs.getString("createdAt"),
                    rs.getInt("flagged") == 1, rs.getInt("helpfulCount"), rs.getInt("unhelpfulCount")));
            }
        }
        return list;
    }

    public static List<Review> getAllReviews() throws Exception {
        List<Review> list = new ArrayList<>();
        String sql =
            "SELECT r.reviewID, r.productID, p.name AS productName, r.customerEmail," +
            "       COALESCE(u.name, r.customerEmail) AS customerName," +
            "       r.rating, r.comment, r.createdAt, r.flagged," +
            "       COALESCE(h.helpfulCount, 0) AS helpfulCount," +
            "       COALESCE(u2.unhelpfulCount, 0) AS unhelpfulCount" +
            " FROM REVIEWS r" +
            " JOIN PRODUCTS p ON p.productID = r.productID" +
            " LEFT JOIN USERS u ON u.email = r.customerEmail" +
            " LEFT JOIN (SELECT reviewID, COUNT(*) AS helpfulCount FROM REVIEW_HELPFUL WHERE helpful=1 GROUP BY reviewID) h ON h.reviewID=r.reviewID" +
            " LEFT JOIN (SELECT reviewID, COUNT(*) AS unhelpfulCount FROM REVIEW_HELPFUL WHERE helpful=0 GROUP BY reviewID) u2 ON u2.reviewID=r.reviewID" +
            " ORDER BY r.createdAt DESC;";
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Review(rs.getInt("reviewID"), rs.getInt("productID"), rs.getString("productName"),
                    rs.getString("customerEmail"), rs.getString("customerName"),
                    rs.getInt("rating"), rs.getString("comment"), rs.getString("createdAt"),
                    rs.getInt("flagged") == 1, rs.getInt("helpfulCount"), rs.getInt("unhelpfulCount")));
            }
        }
        return list;
    }

    public static List<Review> getReviewsByCustomer(String email) throws Exception {
        List<Review> list = new ArrayList<>();
        String sql =
            "SELECT r.reviewID, r.productID, p.name AS productName, r.customerEmail," +
            "       COALESCE(u.name, r.customerEmail) AS customerName," +
            "       r.rating, r.comment, r.createdAt, r.flagged," +
            "       COALESCE(h.helpfulCount, 0) AS helpfulCount," +
            "       COALESCE(u2.unhelpfulCount, 0) AS unhelpfulCount" +
            " FROM REVIEWS r" +
            " JOIN PRODUCTS p ON p.productID = r.productID" +
            " LEFT JOIN USERS u ON u.email = r.customerEmail" +
            " LEFT JOIN (SELECT reviewID, COUNT(*) AS helpfulCount FROM REVIEW_HELPFUL WHERE helpful=1 GROUP BY reviewID) h ON h.reviewID=r.reviewID" +
            " LEFT JOIN (SELECT reviewID, COUNT(*) AS unhelpfulCount FROM REVIEW_HELPFUL WHERE helpful=0 GROUP BY reviewID) u2 ON u2.reviewID=r.reviewID" +
            " WHERE r.customerEmail=? ORDER BY r.createdAt DESC;";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Review(rs.getInt("reviewID"), rs.getInt("productID"), rs.getString("productName"),
                    rs.getString("customerEmail"), rs.getString("customerName"),
                    rs.getInt("rating"), rs.getString("comment"), rs.getString("createdAt"),
                    rs.getInt("flagged") == 1, rs.getInt("helpfulCount"), rs.getInt("unhelpfulCount")));
            }
        }
        return list;
    }

    public static double getAverageRating(int productID) throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT AVG(CAST(rating AS REAL)) FROM REVIEWS WHERE productID=?;")) {
            ps.setInt(1, productID);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // ── Dev entrypoint ─────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        System.out.println(loginchecker("bekabek@gmail.com", "Beka2008"));
    }
}
