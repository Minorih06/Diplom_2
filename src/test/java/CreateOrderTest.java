import api.*;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;

public class CreateOrderTest {

    static Faker faker = new Faker();
    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();

    private String accessToken = null;

    OrderApi orderApi = new OrderApi();
    UserApi userApi = new UserApi();

    static IngredientsApi ingredientApi = new IngredientsApi();
    private static final String FLUORESCENT_BUN = ingredientApi.getIdIngredient("Флюоресцентная булка R2-D3");
    private static final String BEEF_METEORITE = ingredientApi.getIdIngredient("Говяжий метеорит (отбивная)");

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
    }

    @Test
    @DisplayName("Можно создать заказ c ингредиентами без авторизации")
    @Description("Ожидаем, что заказ создан. Код ответа: 200 ОК")
    public void createOrderNonAuthorizedUserTest() {
        Order order = new Order(Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE));
        Response response = orderApi.createOrder(order, null);
        checkedStatusResponse(response, SC_OK);
        checkedBodySuccessfulResponse(response, false);
    }

    @Test
    @DisplayName("Можно создать заказ авторизованному пользователю")
    @Description("Ожидаем, что заказ создан. Код ответа: 200 ОК")
    public void createOrderAuthorizedUserTest() {
        User user = new User(EMAIL, PASSWORD, NAME);
        Response responseCreateUser = userApi.createUser(user);
        accessToken = userApi.getAccessToken(responseCreateUser);

        Order order = new Order(Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE));
        Response response = orderApi.createOrder(order, accessToken);
        checkedStatusResponse(response, SC_OK);
        checkedBodySuccessfulResponse(response, true);
    }

    @Test
    @DisplayName("Нельзя создать заказ с неверным хешем ингредиентов")
    @Description("Ожидаем, что заказ не создан. Код ошибки: 500 Internal Server Error")
    public void createOrderIncorrectHasIngredientsTest() {
        Order order = new Order(Arrays.asList(String.valueOf(faker.hashCode()), BEEF_METEORITE));
        Response response = orderApi.createOrder(order, null);
        checkedStatusResponse(response, SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Нельзя создать заказ без ингредиентов")
    @Description("Ожидаем, что заказ не создан. Код ошибки: 400 Bad Request.")
    public void createOrderNonIngredients() {
        Order order = new Order(new ArrayList<>());
        Response response = orderApi.createOrder(order, null);
        checkedStatusResponse(response, SC_BAD_REQUEST);
        checkedBodyResponse(response, false, "Ingredient ids must be provided");
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа успешного создания заказа")
    public void checkedBodySuccessfulResponse(Response response, boolean shouldAuthorization) {
        int statusCode = response.getStatusCode();
        if (statusCode == SC_OK) {
            response.then().assertThat()
                    .body("name", not(emptyOrNullString()))
                    .body("order.number", not(emptyOrNullString()))
                    .body("success", is(true));
        } else if (statusCode == SC_OK && shouldAuthorization) {
            response.then().assertThat()
                    .body("order._id", not(emptyOrNullString()))
                    .body("order.owner.email", is(EMAIL));
        }
    }

    @Step("Проверка тела ответа")
    public void checkedBodyResponse(Response response, boolean successExpected, String messageExpected) {
        response.then().assertThat()
                .body("success", is(successExpected))
                .body("message", is(messageExpected));
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            userApi.deleteUser(accessToken);
        }
    }
}
