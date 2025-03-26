package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.junit.Assert.assertTrue;

public class AssertionSteps {

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа невалидного запроса")
    public void checkedBodyInvalidResponse(Response response, boolean successExpected, String messageExpected) {
        response.then().assertThat()
                .body("success", is(successExpected))
                .body("message", is(messageExpected));
    }

    @Step("Проверка тела ответа успешной смены информации пользователя")
    public void checkedBodySuccessfulResponseChangeInformation(Response response, String email, String name) {
        response.then().assertThat()
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name));
    }

    @Step("Проверка тела ответа успешного создания заказа с авторизацией")
    public void checkedBodyResponseSuccessfulCreateOrder(Response response, String email) {
        response.then().assertThat()
                .body("order._id", not(emptyOrNullString()))
                .body("order.owner.email", is(email));
    }

    @Step("Проверка тела ответа успешного создания заказа без авторизации")
    public void checkedBodyResponseSuccessfulCreateOrder(Response response) {
        response.then().assertThat()
                .body("name", not(emptyOrNullString()))
                .body("order.number", not(emptyOrNullString()))
                .body("success", is(true));

    }

    @Step("Проверка тела ответа на успешную авторизацию/создание пользователя")
    public void checkedBodyResponseSuccessfulAuthorization(Response response, String email, String name) {
        response.then().assertThat()
                .body("success", is(true))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", not(emptyOrNullString()))
                .body("user.email", is(email))
                .body("user.name", is(name));
    }

    @Step("Проверяем, что такой id заказа есть в списке заказов")
    public void checkedIdOrdersList(String expectedOrderId, Response receivingOrdersResponse) {
        String json = receivingOrdersResponse.asString();
        List<String> orderIds = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray orders = jsonObject.getJSONArray("orders");

        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            orderIds.add(order.getString("_id"));
        }

        assertTrue(orderIds.contains(expectedOrderId));
    }

}
