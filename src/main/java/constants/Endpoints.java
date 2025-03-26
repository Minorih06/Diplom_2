package constants;

import lombok.Getter;

import static api.ProjectURL.URL;

@Getter
public enum Endpoints {
    // Endpoints для работы с User
    CREATE_USER_REQUEST(URL + "/api/auth/register"),
    LOGIN_USER_REQUEST(URL + "/api/auth/login"),
    DELETE_USER_REQUEST(URL + "/api/auth/user"),
    CHANGE_INFORMATION_USER_REQUEST(URL + "/api/auth/user"),

    // Endpoint для работы с Orders
    ORDERS_REQUEST(URL + "/api/orders"),

    // Endpoint для работы с Ingredients
    INGREDIENTS_REQUEST(URL + "/api/ingredients");

    private final String url;

    Endpoints(String url) {
        this.url = url;
    }
}
