import api.*;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.FakeUser;
import model.Order;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import steps.AssertionSteps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import static org.junit.Assert.assertTrue;

public class ReceivingUserOrdersTest {
    FakeUser fakeUser = new FakeUser();

    private String accessToken;

    IngredientsApi ingredientApi = new IngredientsApi();
    private final String fluorescentBun = ingredientApi.getIdIngredient("Флюоресцентная булка R2-D3");
    private final String beefMeteorite = ingredientApi.getIdIngredient("Говяжий метеорит (отбивная)");

    UserApi userApi = new UserApi();
    OrderApi orderApi = new OrderApi();

    private boolean shouldDeleteUser = false;

    AssertionSteps assertionSteps = new AssertionSteps();

    @Test
    @DisplayName("Проверка получения заказов конкретного авторизованного пользователя")
    @Description("Ожидаем, что список заказов получен, статус код: 200 ОК, в полученном списке присутствует ранее созданные заказ")
    public void receivingOrdersAuthorizedUserTest() {
        User user = new User(fakeUser.getEmail(), fakeUser.getPassword(), fakeUser.getName());
        Response createUser = userApi.createUser(user);
        accessToken = userApi.getAccessToken(createUser);
        shouldDeleteUser = true;

        Order order = new Order(Arrays.asList(fluorescentBun, beefMeteorite));
        Response createOrder = orderApi.createOrder(order, accessToken);
        String idOrder = orderApi.getIdOrder(createOrder);

        Response receivingOrders = orderApi.receivingUserOrders(accessToken);
        assertionSteps.checkedStatusResponse(receivingOrders, SC_OK);
        assertionSteps.checkedIdOrdersList(idOrder, receivingOrders);
    }

    @Test
    @DisplayName("Нельзя получить список заказов конкретного пользователя не авторизовавшись")
    @Description("Ожидаем, что список заказов не будет получен, статус код: 401 Unauthorized, тело ответа: {\"success\":false,\"message\":\"You should be authorised\"}")
    public void receivingOrdersUnauthorizedUserTest() {
        Response response = orderApi.receivingUserOrders();
        assertionSteps.checkedStatusResponse(response, SC_UNAUTHORIZED);
        assertionSteps.checkedBodyInvalidResponse(response, false, "You should be authorised");
    }

    @After
    public void deleteUser() {
        if (shouldDeleteUser) {
            userApi.deleteUser(accessToken);
        }
    }
}
