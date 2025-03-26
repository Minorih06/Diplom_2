import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.FakeUser;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.AssertionSteps;

import static org.apache.http.HttpStatus.SC_OK;

public class LoginUserTest {

    private FakeUser fakeUser;

    private String accessToken;

    UserApi userApi = new UserApi();
    private User user;

    AssertionSteps assertionSteps = new AssertionSteps();

    @Before
    public void setUp() {
        fakeUser = new FakeUser();
        user = new User(fakeUser.getEmail(), fakeUser.getPassword(), fakeUser.getName());
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
    }

    @Test
    @DisplayName("Можно авторизоваться зарегистрированному пользователю")
    @Description("Ожидаем, что пользователь будет авторизован. Статус ответа: 200 ОК. В теле ответа вернётся информация о пользователе и токен авторизации.")
    public void userLoginTest() {
        Response response = userApi.loginUser(user);
        assertionSteps.checkedStatusResponse(response, SC_OK);
        assertionSteps.checkedBodyResponseSuccessfulAuthorization(response, fakeUser.getEmail(), fakeUser.getName());
    }

    @After
    public void deleteUser() {
        userApi.deleteUser(accessToken);
    }
}
