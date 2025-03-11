import io.qameta.allure.Description;
import model.User;
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

@RunWith(Parameterized.class)
public class ParametrizedLoginUserTest {

    static Faker faker = new Faker();

    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();

    private String accessToken;

    UserApi userApi = new UserApi();

    private final User user;
    private final String testName;

    public ParametrizedLoginUserTest(User user, String testName) {
        this.user = user;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Object[][] getUser() {
        return new Object[][] {
                {new User(EMAIL, faker.internet().password()), "с неверным password"},
                {new User(faker.internet().emailAddress(), PASSWORD), "с неверным email"}
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
    @DisplayName("Нельзя авторизоваться с неверными данными")
    @Description("Ожидаем, что авторизация не произойдёт. Код ответа: 401 Unauthorized. В теле ответа вернётся информация об ошибке.")
    public void userLoginTest() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName("Нельзя авторизоваться " + testName));

        Response response = userApi.loginUser(user);
        checkedStatusResponse(response, SC_UNAUTHORIZED);
        checkedBodyResponse(response, false, "email or password are incorrect");
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа")
    public void checkedBodyResponse(Response response, boolean successExpected, String messageExpected) {
        response.then().assertThat()
                .body("success", is(successExpected))
                .body("message", is(messageExpected));
    }

    @After
    public void deleteUser() {
        userApi.deleteUser(accessToken);
    }
}
