import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.User;
import model.FakeUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.AssertionSteps;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;

public class CreateUserTest {

    private FakeUser fakeUser;

    UserApi userApi = new UserApi();
    private User user;

    private String accessToken = null;

    AssertionSteps assertionSteps = new AssertionSteps();

    @Before
    public void setUp() {
        fakeUser = new FakeUser();
        user = new User(fakeUser.getEmail(), fakeUser.getPassword(), fakeUser.getName());
    }

    @Test
    @DisplayName("Можно создать уникального пользователя")
    @Description("Ожидаем, что пользователь будет создан. Статус ответа: 200 ОК. В ответе вернётся информация о пользователе и токен авторизации.")

    public void createUserTest() {
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
        assertionSteps.checkedStatusResponse(response, SC_OK);
        assertionSteps.checkedBodyResponseSuccessfulAuthorization(response, fakeUser.getEmail(), fakeUser.getName());
    }

    @Test
    @DisplayName("Нельзя создать пользователя, который уже зарегистрирован")
    @Description("Ожидаем что пользователь не будет создан. Статус ответа: 403 Forbidden. В теле вернётся информация об ошибке.")
    public void cannotCreateUserWhoIsAlreadyRegistered() {
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
        Response responseCreateNewUser = userApi.createUser(user);
        assertionSteps.checkedStatusResponse(responseCreateNewUser, SC_FORBIDDEN);
        assertionSteps.checkedBodyInvalidResponse(responseCreateNewUser, false, "User already exists");
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            userApi.deleteUser(accessToken);
        }
    }
}
