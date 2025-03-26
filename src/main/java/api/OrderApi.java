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
            return given().header("Authorization", accessToken).header("Content-type", "application/json").and().body(order).when().post(Endpoints.ORDERS_REQUEST.getUrl());
        } else {
            return given().header("Content-type", "application/json").and().body(order).when().post(Endpoints.ORDERS_REQUEST.getUrl());
        }
    }

    @Step("Получение заказов конкретного пользователя")
    public Response receivingUserOrders(String accessToken) {
        return given().header("Authorization", accessToken).header("Content-type", "application/json").get(Endpoints.ORDERS_REQUEST.getUrl());
    }

    @Step("Получение заказов конкретного пользователя без авторизации")
    public Response receivingUserOrders() {
        return given().header("Content-type", "application/json").get(Endpoints.ORDERS_REQUEST.getUrl());
    }

    @Step("Получение id заказа")
    public String getIdOrder(Response response) {
        return response.jsonPath().getString("order._id");
    }
}
