import io.qameta.allure.Description;
import model.FakeUser;
import model.User;
import api.UserApi;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import steps.AssertionSteps;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class ParametrizedCreateUserTest {

    static FakeUser fakeUser = new FakeUser();

    UserApi userApi = new UserApi();

    AssertionSteps assertionSteps = new AssertionSteps();

    private final User user;
    private final String testName;

    public ParametrizedCreateUserTest(User user, String testName) {
        this.user = user;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Object[][] getUser() {
        return new Object[][] {
                {User.builder().password(fakeUser.getPassword()).name(fakeUser.getName()).build(), "не заполнив email"},
                {User.builder().email(fakeUser.getEmail()).name(fakeUser.getName()).build(), "не заполнив password"},
                {User.builder().email(fakeUser.getEmail()).password(fakeUser.getPassword()).build(), "не заполнив name"}
        };
    }

    @Test
    @DisplayName("Проверка создания пользователя, не заполнив все обязательные поля")
    @Description("Ожидаем, что пользователь не будет создан. Код ответа: 403 Forbidden. В теле ответа вернётся информация об ошибке.")
    public void cannotCreateUserWithoutFillingRequiredFields() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName("Нельзя создать пользователя, " + testName));

        Response response = userApi.createUser(user);
        assertionSteps.checkedStatusResponse(response, SC_FORBIDDEN);
        assertionSteps.checkedBodyInvalidResponse(response, false, "Email, password and name are required fields");
    }

}
