import api.User;
import api.UserApi;
import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.emptyOrNullString;

@RunWith(Parameterized.class)
public class ParametrizedLoginUserTest {

    static Faker faker = new Faker();

    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();
    private static final String INCORRECT_FIELDS_BODY_RESPONSE =  "{\"success\":false,\"message\":\"email or password are incorrect\"}";

    private String accessToken;

    UserApi userApi = new UserApi();

    private final User user;
    private final int expectedStatusCode;
    private final String expectedResponseBody;
    private final String testName;

    public ParametrizedLoginUserTest(User user, int statusCode, String expectedResponseBody, String testName) {
        this.user = user;
        this.expectedStatusCode = statusCode;
        this.expectedResponseBody = expectedResponseBody;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{index}: {3}")
    public static Object[][] getUser() {
        return new Object[][] {
                {new User(EMAIL, PASSWORD), SC_OK, "", "Успешная авторизация существующего пользователя"},
                {new User(EMAIL, faker.internet().password()), SC_UNAUTHORIZED, INCORRECT_FIELDS_BODY_RESPONSE, "Нельзя авторизоваться с неверным password"},
                {new User(faker.internet().emailAddress(), PASSWORD), SC_UNAUTHORIZED, INCORRECT_FIELDS_BODY_RESPONSE, "Нельзя авторизоваться с неверным email"}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
        User user = new User(EMAIL, PASSWORD, NAME);
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
    }

    @Test
    @DisplayName("Проверка авторизации пользователя")
    public void userLoginTest() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName(testName));

        Response response = userApi.loginUser(user);
        checkedStatusResponse(response, expectedStatusCode);
        checkedBodyResponse(response, expectedResponseBody);
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа")
    public void checkedBodyResponse(Response response, String responseBody) {
        int statusCode = response.getStatusCode();
        if (statusCode == SC_OK) {
            response.then()
                    .body("success", equalTo(true))
                    .body("accessToken", startsWith("Bearer "))
                    .body("refreshToken", not(emptyOrNullString()))
                    .body("user.email", equalTo(EMAIL))
                    .body("user.name", equalTo(NAME));
        } else {
            response.then().body(equalTo(responseBody));
        }

    }

    @After
    public void deleteUser() {
        userApi.deleteUser(accessToken);
    }
}
