import io.qameta.allure.Description;
import model.FakeUser;
import model.User;
import api.UserApi;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import steps.AssertionSteps;
import utilits.FakerUtility;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class ParametrizedLoginUserTest {

    static FakerUtility fakerUtility = new FakerUtility();
    static FakeUser fakeUser = new FakeUser();

    private String accessToken;

    UserApi userApi = new UserApi();

    AssertionSteps assertionSteps = new AssertionSteps();

    private final User user;
    private final String testName;

    public ParametrizedLoginUserTest(User user, String testName) {
        this.user = user;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Object[][] getUser() {
        return new Object[][] {
                {User.builder().email(fakeUser.getEmail()).password(fakerUtility.getPassword()).build(), "с неверным password"},
                {User.builder().email(fakerUtility.getEmailAddress()).password(fakeUser.getPassword()).build(), "с неверным email"}
        };
    }

    @Before
    public void setUp() {
        User user = new User(fakeUser.getEmail(), fakeUser.getPassword(), fakeUser.getName());
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверными данными")
    @Description("Ожидаем, что авторизация не произойдёт. Код ответа: 401 Unauthorized. В теле ответа вернётся информация об ошибке.")
    public void userLoginTest() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName("Нельзя авторизоваться " + testName));

        Response response = userApi.loginUser(user);
        assertionSteps.checkedStatusResponse(response, SC_UNAUTHORIZED);
        assertionSteps.checkedBodyInvalidResponse(response, false, "email or password are incorrect");
    }

    @After
    public void deleteUser() {
        userApi.deleteUser(accessToken);
    }
}
