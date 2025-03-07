import api.*;
import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;

@RunWith(Parameterized.class)
public class ParametrizedCreateOrderTest {

    static Faker faker = new Faker();
    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();

    private String accessToken;

    OrderApi orderApi = new OrderApi();
    UserApi userApi = new UserApi();

    static IngredientsApi ingredientApi = new IngredientsApi();
    private static final String FLUORESCENT_BUN = ingredientApi.getIdIngredient("Флюоресцентная булка R2-D3");
    private static final String BEEF_METEORITE = ingredientApi.getIdIngredient("Говяжий метеорит (отбивная)");

    private static final String INGREDIENT_MUST_BODY_RESPONSE = "{\"success\":false,\"message\":\"Ingredient ids must be provided\"}";

    private final List<String> ingredientsList;
    private final int expectedStatusCode;
    private final String expectedResponseBody;
    private final boolean shouldAuthorization;
    private final String testName;

    private boolean shouldDeleteUser = false;

    public ParametrizedCreateOrderTest(List<String> ingredientsList, int expectedStatusCode, String expectedResponseBody, boolean shouldAuthorization, String testName) {
        this.ingredientsList = ingredientsList;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedResponseBody = expectedResponseBody;
        this.shouldAuthorization = shouldAuthorization;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{index}: {4}")
    public static Object[][] getUser() {
        return new Object[][] {
                {Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE), SC_OK, "", false, "Можно создать заказ c ингредиентами без авторизации"},
                {Arrays.asList(FLUORESCENT_BUN, BEEF_METEORITE), SC_OK, "", true, "Можно создать заказ авторизованному пользователю"},
                {Arrays.asList(faker.hashCode()), SC_INTERNAL_SERVER_ERROR, "", false, "Нельзя создать заказ с неверным хешем ингредиентов"},
                {new ArrayList<>(), SC_BAD_REQUEST, INGREDIENT_MUST_BODY_RESPONSE, false, "Нельзя создать заказ без ингредиентов" }
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
    }

    @Test
    public void createOrderTest() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName(testName));

        if (shouldAuthorization) {
            User user = new User(EMAIL, PASSWORD, NAME);
            Response response = userApi.createUser(user);
            accessToken = userApi.getAccessToken(response);
            shouldDeleteUser = true;
        } else {
            accessToken = null;
        }

        Order order = new Order(ingredientsList);
        Response response = orderApi.createOrder(order, accessToken);
        checkedStatusResponse(response, expectedStatusCode);

        if (expectedStatusCode != SC_INTERNAL_SERVER_ERROR) {
            checkedBodyResponse(response, expectedResponseBody, shouldAuthorization);
        }
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа")
    public void checkedBodyResponse(Response response, String responseBody, boolean shouldAuthorization) {
        int statusCode = response.getStatusCode();
        if (statusCode == SC_OK) {
            response.then()
                    .body("name", not(emptyOrNullString()))
                    .body("order.number", not(emptyOrNullString()))
                    .body("success", equalTo(true));
        } else if (statusCode == SC_OK && shouldAuthorization) {
            response.then()
                    .body("order._id", not(emptyOrNullString()))
                    .body("order.owner.email", equalTo(EMAIL));
        } else {
            response.then().body(equalTo(responseBody));
        }
    }

    @After
    public void deleteUser() {
        if (shouldDeleteUser) {
            userApi.deleteUser(accessToken);
        }
    }
}
