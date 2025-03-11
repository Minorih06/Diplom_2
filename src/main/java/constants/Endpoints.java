package constants;

public enum Endpoints {
    // Endpoints для работы с User
    CREATE_USER_REQUEST("/api/auth/register"),
    LOGIN_USER_REQUEST("/api/auth/login"),
    DELETE_USER_REQUEST("/api/auth/user"),
    CHANGE_INFORMATION_USER_REQUEST("/api/auth/user"),

    // Endpoint для работы с Orders
    ORDERS_REQUEST("/api/orders"),

    // Endpoint для работы с Ingredients
    INGREDIENTS_REQUEST("/api/ingredients");

    private final String url;

    Endpoints(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return url;
    }
}
