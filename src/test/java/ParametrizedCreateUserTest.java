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
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;

@RunWith(Parameterized.class)
public class ParametrizedCreateUserTest {

    static Faker faker = new Faker();
    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();
    private static final String NO_REQUIRED_FIELDS_BODY_RESPONSE =  "{\"success\":false,\"message\":\"Email, password and name are required fields\"}";
    private static final String RE_CREATE_USER_BODY_RESPONSE = "{\"success\":false,\"message\":\"User already exists\"}";

    UserApi userApi = new UserApi();

    private final User user;
    private final int expectedStatusCode;
    private final String expectedResponseBody;
    private final String testName;
    private final boolean reCreatingUser; //необходимо ли создание второго пользователя?
    private final boolean shouldDeleteUser; //необходимо ли удаление созданного пользователя?

    public ParametrizedCreateUserTest(User user, int expectedStatusCode, String expectedResponseBody, String testName, boolean reCreatingUser, boolean shouldDeleteUser) {
        this.user = user;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedResponseBody = expectedResponseBody;
        this.testName = testName;
        this.reCreatingUser = reCreatingUser;
        this.shouldDeleteUser = shouldDeleteUser;
    }

    @Parameterized.Parameters(name = "{index}: {3}")
    public static Object[][] getUser() {
        return new Object[][] {
                {new User(EMAIL, PASSWORD, NAME), SC_OK, "", "Можно создать уникального пользователя", false, true},
                {new User(false, PASSWORD, NAME), SC_FORBIDDEN, NO_REQUIRED_FIELDS_BODY_RESPONSE, "Нельзя создать пользователя, не заполнив email", false, false},
                {new User(EMAIL, false, NAME), SC_FORBIDDEN, NO_REQUIRED_FIELDS_BODY_RESPONSE, "Нельзя создать пользователя, не заполнив password", false, false},
                {new User(EMAIL, PASSWORD), SC_FORBIDDEN, NO_REQUIRED_FIELDS_BODY_RESPONSE, "Нельзя создать пользователя, не заполнив name", false, false},
                {new User(EMAIL, PASSWORD, NAME), SC_FORBIDDEN, RE_CREATE_USER_BODY_RESPONSE, "Нельзя создать пользователя, который уже зарегистрирован", true, true}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
    }

    @Test
    @DisplayName("Проверка создания пользователя")
    public void cannotCreateUserWithoutFillingRequiredFields() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName(testName));

        Response response;
        if (reCreatingUser) {
            userApi.createUser(user); //создаём первого пользователя
            response = userApi.createUser(user); //пытаемся создать второго пользователя
        } else {
            response = userApi.createUser(user); //создаём уникального пользователя
        }

        checkedStatusResponse(response, expectedStatusCode); //проверяем код ответа
        checkedBodyResponse(response, expectedResponseBody); //проверяем тело ответа
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
                    .body("user.email", equalTo(EMAIL))
                    .body("user.name", equalTo(NAME))
                    .body("accessToken", startsWith("Bearer "))
                    .body("refreshToken", not(emptyOrNullString()));
        } else  {
            response.then().body(equalTo(responseBody));
        }
    }

    @After
    public void deleteUser() {
        if (shouldDeleteUser) {
            User user = new User(EMAIL, PASSWORD);
            Response response = userApi.loginUser(user);
            String accessToken = userApi.getAccessToken(response);
            userApi.deleteUser(accessToken);
        }
    }
}
