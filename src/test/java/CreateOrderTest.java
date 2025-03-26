import api.*;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.FakeUser;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Test;
import steps.AssertionSteps;
import utilits.FakerUtility;

import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.http.HttpStatus.*;

public class CreateOrderTest {

    FakerUtility fakerUtility = new FakerUtility();
    FakeUser fakeUser = new FakeUser();

    private String accessToken = null;

    OrderApi orderApi = new OrderApi();
    UserApi userApi = new UserApi();

    static IngredientsApi ingredientApi = new IngredientsApi();
    private static final String FLUORESCENT_BUN = ingredientApi.getIdIngredient("Флюоресцентная булка R2-D3");
    private static final String BEEF_METEORITE = ingredientApi.getIdIngredient("Говяжий метеорит (отбивная)");

    AssertionSteps assertionSteps = new AssertionSteps();

    @Test
    @DisplayName("Можно создать заказ c ингредиентами без авторизации")
    @Description("Ожидаем, что заказ создан. Код ответа: 200 ОК")
    public void createOrderNonAuthorizedUserTest() {
        Order order = new Order(Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE));
        Response response = orderApi.createOrder(order, null);
        assertionSteps.checkedStatusResponse(response, SC_OK);
        assertionSteps.checkedBodyResponseSuccessfulCreateOrder(response);
    }

    @Test
    @DisplayName("Можно создать заказ авторизованному пользователю")
    @Description("Ожидаем, что заказ создан. Код ответа: 200 ОК")
    public void createOrderAuthorizedUserTest() {
        User user = new User(fakeUser.getEmail(), fakeUser.getPassword(), fakeUser.getName());
        Response responseCreateUser = userApi.createUser(user);
        accessToken = userApi.getAccessToken(responseCreateUser);

        Order order = new Order(Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE));
        Response response = orderApi.createOrder(order, accessToken);
        assertionSteps.checkedStatusResponse(response, SC_OK);
        assertionSteps.checkedBodyResponseSuccessfulCreateOrder(response, fakeUser.getEmail());
    }

    @Test
    @DisplayName("Нельзя создать заказ с неверным хешем ингредиентов")
    @Description("Ожидаем, что заказ не создан. Код ошибки: 500 Internal Server Error")
    public void createOrderIncorrectHasIngredientsTest() {
        Order order = new Order(Arrays.asList(String.valueOf(fakerUtility.getHashCode()), BEEF_METEORITE));
        Response response = orderApi.createOrder(order, null);
        assertionSteps.checkedStatusResponse(response, SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Нельзя создать заказ без ингредиентов")
    @Description("Ожидаем, что заказ не создан. Код ошибки: 400 Bad Request.")
    public void createOrderNonIngredients() {
        Order order = new Order(new ArrayList<>());
        Response response = orderApi.createOrder(order, null);
        assertionSteps.checkedStatusResponse(response, SC_BAD_REQUEST);
        assertionSteps.checkedBodyInvalidResponse(response, false, "Ingredient ids must be provided");
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            userApi.deleteUser(accessToken);
        }
    }
}
