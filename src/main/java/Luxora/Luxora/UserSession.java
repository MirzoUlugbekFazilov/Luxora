package Luxora.Luxora;

/**
 * Simple static session holder – survives navigation between FXML screens.
 */
public class UserSession {

    private static String name  = "";
    private static String email = "";
    private static String role  = "";

    public static void set(String name, String email, String role) {
        UserSession.name  = name  == null ? "" : name;
        UserSession.email = email == null ? "" : email;
        UserSession.role  = role  == null ? "" : role;
    }

    public static String getName()  { return name; }
    public static String getEmail() { return email; }
    public static String getRole()  { return role; }

    public static void clear() {
        name = ""; email = ""; role = "";
    }
}
