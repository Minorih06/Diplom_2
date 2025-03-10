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
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyOrNullString;

public class CreateUserTest {

    Faker faker = new Faker();
    private final String email = faker.internet().emailAddress();
    private final String password = faker.internet().password();
    private final String name = faker.name().firstName();

    UserApi userApi = new UserApi();
    private User user;

    private String accessToken = null;

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
        user = new User(email, password, name);
    }

    @Test
    @DisplayName("Можно создать уникального пользователя")
    @Description("Ожидаем, что пользователь будет создан. Статус ответа: 200 ОК. В ответе вернётся информация о пользователе и токен авторизации.")

    public void createUserTest() {
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
        checkedStatusResponse(response, SC_OK);
        checkedBodySuccessfulResponse(response);
    }

    @Test
    @DisplayName("Нельзя создать пользователя, который уже зарегистрирован")
    @Description("Ожидаем что пользователь не будет создан. Статус ответа: 403 Forbidden. В теле вернётся информация об ошибке.")
    public void cannotCreateUserWhoIsAlreadyRegistered() {
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
        Response responseCreateNewUser = userApi.createUser(user);
        checkedStatusResponse(responseCreateNewUser, SC_FORBIDDEN);
        checkedBodyResponse(responseCreateNewUser, false, "User already exists");
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа успешного создания пользователя")
    public void checkedBodySuccessfulResponse(Response response) {
        response.then().assertThat()
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", not(emptyOrNullString()));
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
