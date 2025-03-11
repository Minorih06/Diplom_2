package api;

import constants.Endpoints;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Order;

import static io.restassured.RestAssured.given;

public class OrderApi {

    @Step("Создание заказа")
    public Response createOrder(Order order, String accessToken) {
        if (accessToken != null) {
            return given().header("Authorization", accessToken).header("Content-type", "application/json").and().body(order).when().post(Endpoints.ORDERS_REQUEST.toString());
        } else {
            return given().header("Content-type", "application/json").and().body(order).when().post(Endpoints.ORDERS_REQUEST.toString());
        }
    }

    @Step("Получение заказов конкретного пользователя")
    public Response receivingUserOrders(String accessToken) {
        if (accessToken != null) {
            return given().header("Authorization", accessToken).header("Content-type", "application/json").get(Endpoints.ORDERS_REQUEST.toString());
        } else {
            return given().header("Content-type", "application/json").get(Endpoints.ORDERS_REQUEST.toString());
        }
    }

    @Step("Получение id заказа")
    public String getIdOrder(Response response) {
        return response.jsonPath().getString("order._id");
    }
}
