package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderApi {
    private static final String ORDERS_REQUEST = "/api/orders";

    @Step("Создание заказа")
    public Response createOrder(Order order, String accessToken) {
        if (accessToken != null) {
            return given().header("Authorization", accessToken).header("Content-type", "application/json").and().body(order).when().post(ORDERS_REQUEST);
        } else {
            return given().header("Content-type", "application/json").and().body(order).when().post(ORDERS_REQUEST);
        }
    }

    @Step("Получение заказов конкретного пользователя")
    public Response receivingUserOrders(String accessToken) {
        if (accessToken != null) {
            return given().header("Authorization", accessToken).header("Content-type", "application/json").get(ORDERS_REQUEST);
        } else {
            return given().header("Content-type", "application/json").get(ORDERS_REQUEST);
        }
    }

    @Step("Получение id заказа")
    public String getIdOrder(Response response) {
        return response.jsonPath().getString("order._id");
    }
}
