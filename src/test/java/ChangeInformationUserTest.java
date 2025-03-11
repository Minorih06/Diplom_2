import model.User;
import api.UserApi;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.is;

public class ChangeInformationUserTest {

    Faker faker = new Faker();
    private final String email = faker.internet().emailAddress();
    private final String password = faker.internet().password();
    private final String name = faker.name().firstName();

    private String accessToken;

    UserApi userApi = new UserApi();

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
        User user = new User(email, password, name);
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
    }

    @Test
    @DisplayName("Можно изменить email авторизованному пользователю")
    @Description("Ожидаем, что email будет изменён, код ответа: 200 ОК, тело ответа соответствует структуре: {\"success\":true, \"user\": {\"email\":\"\", \"name\":\"\" } }")
    public void changeEmailTest() {
        String newEmail = faker.internet().emailAddress();
        User changeData = new User(newEmail);
        Response response = userApi.setChangeInformationUser(accessToken, changeData);
        checkedStatusResponse(response, SC_OK);
        checkedBodySuccessfulResponse(response, newEmail, name);
    }

    @Test
    @DisplayName("Можно изменить name авторизованному пользователю")
    @Description("Ожидаем, что name будет изменён, код ответа: 200 ОК, тело ответа соответствует структуре: {\"success\":true, \"user\": {\"email\":\"\", \"name\":\"\" } }")
    public void changeNameTest() {
        String newName = faker.name().firstName();
        User changeData = new User(false, false, newName);
        Response response = userApi.setChangeInformationUser(accessToken, changeData);
        checkedStatusResponse(response, SC_OK);
        checkedBodySuccessfulResponse(response, email, newName);
    }

    @Test
    @DisplayName("Нельзя изменить информацию для неавторизованного пользователя")
    @Description("Ожидаем, что информация не будет изменена, код ответ: 401 Unauthorized, тело ответа: {\"success\":false,\"message\":\"You should be authorised\"}")
    public void mustNotChangeInformationUnauthorizedUser() {
        String newEmail = faker.internet().emailAddress();
        User changeData = new User(newEmail);
        Response response = userApi.setChangeInformationUser(null, changeData);
        checkedStatusResponse(response, SC_UNAUTHORIZED);
        checkedBodyResponse(response, false, "You should be authorised");
    }

    @Test
    @DisplayName("Нельзя передать в запросе email, который уже используется")
    @Description("Ожидаем код ответ: 403 Forbidden, тело ответа: {\"success\":false,\"message\":\"User with such email already exists\"}")
    public void mustNotChangeEmailToIdentical() {
        String newEmail = faker.internet().emailAddress();
        User newUser = new User(newEmail, password, name);
        Response createNewUserResponse =userApi.createUser(newUser);
        String accessTokenNewUser = userApi.getAccessToken(createNewUserResponse);

        User changeData = new User(newEmail);
        Response response = userApi.setChangeInformationUser(accessToken, changeData);

        userApi.deleteUser(accessTokenNewUser);

        checkedStatusResponse(response, SC_FORBIDDEN);
        checkedBodyResponse(response, false, "User with such email already exists");
    }

    @Step("Проверка статуса ответа")
    public void checkedStatusResponse(Response response, int code) {
        response.then().statusCode(code);
    }

    @Step("Проверка тела ответа успешной смены информации")
    public void checkedBodySuccessfulResponse(Response response, String email, String name) {
        response.then().assertThat()
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name));
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
