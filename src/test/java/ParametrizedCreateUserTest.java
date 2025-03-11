import io.qameta.allure.Description;
import model.User;
import api.UserApi;
import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static api.ProjectURL.URL;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class ParametrizedCreateUserTest {

    static Faker faker = new Faker();
    private static final String EMAIL = faker.internet().emailAddress();
    private static final String PASSWORD = faker.internet().password();
    private static final String NAME = faker.name().firstName();

    UserApi userApi = new UserApi();

    private final User user;
    private final String testName;

    public ParametrizedCreateUserTest(User user, String testName) {
        this.user = user;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Object[][] getUser() {
        return new Object[][] {
                {new User(false, PASSWORD, NAME), "не заполнив email"},
                {new User(EMAIL, false, NAME), "не заполнив password"},
                {new User(EMAIL, PASSWORD), "не заполнив name"}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = URL;
    }

    @Test
    @DisplayName("Проверка создания пользователя, не заполнив все обязательные поля")
    @Description("Ожидаем, что пользователь не будет создан. Код ответа: 403 Forbidden. В теле ответа вернётся информация об ошибке.")
    public void cannotCreateUserWithoutFillingRequiredFields() {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName("Нельзя создать пользователя, " + testName));

        Response response = userApi.createUser(user);
        checkedStatusResponse(response, SC_FORBIDDEN);
        checkedBodyResponse(response, false, "Email, password and name are required fields");
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
}
