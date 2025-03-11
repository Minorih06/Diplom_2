import api.UserApi;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;

public class LoginUserTest {

    static Faker faker = new Faker();

    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();

    private String accessToken;

    UserApi userApi = new UserApi();
    private User user;

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
        user = new User(EMAIL, PASSWORD, NAME);
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
    }

    @Test
    @DisplayName("Можно авторизоваться зарегистрированному пользователю")
    @Description("Ожидаем, что пользователь будет авторизован. Статус ответа: 200 ОК. В теле ответа вернётся информация о пользователе и токен авторизации.")
    public void userLoginTest() {
        Response response = userApi.loginUser(user);
        checkedStatusResponse(response, SC_OK);
        checkedBodyResponse(response);
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа")
    public void checkedBodyResponse(Response response) {
        response.then().assertThat()
                .body("success", is(true))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", not(emptyOrNullString()))
                .body("user.email", is(EMAIL))
                .body("user.name", is(NAME));
    }

    @After
    public void deleteUser() {
        userApi.deleteUser(accessToken);
    }
}
