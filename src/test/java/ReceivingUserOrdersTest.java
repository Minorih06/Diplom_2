import api.Order;
import api.OrderApi;
import api.User;
import api.UserApi;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertTrue;


public class ReceivingUserOrdersTest {
    static Faker faker = new Faker();
    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();

    private String accessToken;

    private static final String FLUORESCENT_BUN = "61c0c5a71d1f82001bdaaa6d";
    private static final String BEEF_METEORITE = "61c0c5a71d1f82001bdaaa70";
    private static final String RECEIVING_ORDERS_UNAUTHORIZED_USER_RESPONSE_BODY = "{\"success\":false,\"message\":\"You should be authorised\"}";

    UserApi userApi = new UserApi();
    OrderApi orderApi = new OrderApi();

    private boolean shouldDeleteUser = false;

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
    }

    @Test
    @DisplayName("Проверка получения заказов конкретного авторизованного пользователя")
    @Description("Ожидаем, что список заказов получен, статус код: 200 ОК, в полученном списке присутствует ранее созданные заказ")
    public void receivingOrdersAuthorizedUserTest() {
        User user = new User(EMAIL, PASSWORD, NAME);
        Response createUser = userApi.createUser(user);
        accessToken = userApi.getAccessToken(createUser);
        shouldDeleteUser = true;

        Order order = new Order(Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE));
        Response createOrder = orderApi.createOrder(order, accessToken);
        String idOrder = orderApi.getIdOrder(createOrder);

        Response receivingOrders = orderApi.receivingUserOrders(accessToken);
        checkedStatusResponse(receivingOrders, SC_OK);
        checkedIdOrdersList(idOrder, receivingOrders);
    }

    @Test
    @DisplayName("Нельзя получить список заказов конкретного пользователя не авторизовавшись")
    @Description("Ожидаем, что список заказов не будет получен, статус код: 401 Unauthorized, тело ответа: {\"success\":false,\"message\":\"You should be authorised\"}")
    public void receivingOrdersUnauthorizedUserTest() {
        Response response = orderApi.receivingUserOrders(null);
        checkedStatusResponse(response, SC_UNAUTHORIZED);
        checkedBodyResponse(response, RECEIVING_ORDERS_UNAUTHORIZED_USER_RESPONSE_BODY);
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
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

    @Step("Проверка тела ответа")
    public void checkedBodyResponse(Response response, String responseBody) {
        response.then().body(equalTo(responseBody));
    }


    @After
    public void deleteUser() {
        if (shouldDeleteUser) {
            userApi.deleteUser(accessToken);
        }
    }
}
