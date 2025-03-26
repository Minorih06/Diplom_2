import model.FakeUser;
import model.User;
import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.AssertionSteps;
import utilits.FakerUtility;

import static org.apache.http.HttpStatus.*;

public class ChangeInformationUserTest {

    FakerUtility fakerUtility = new FakerUtility();
    FakeUser fakeUser = new FakeUser();

    private String accessToken;

    UserApi userApi = new UserApi();

    AssertionSteps assertionSteps = new AssertionSteps();

    @Before
    public void setUp() {
        User user = new User(fakeUser.getEmail(), fakeUser.getPassword(), fakeUser.getName());
        Response response = userApi.createUser(user);
        accessToken = userApi.getAccessToken(response);
    }

    @Test
    @DisplayName("Можно изменить email авторизованному пользователю")
    @Description("Ожидаем, что email будет изменён, код ответа: 200 ОК, тело ответа соответствует структуре: {\"success\":true, \"user\": {\"email\":\"\", \"name\":\"\" } }")
    public void changeEmailTest() {
        String newEmail = fakerUtility.getEmailAddress();
        User changeData = User.builder()
                .email(newEmail)
                .build();
        Response response = userApi.setChangeInformationUser(accessToken, changeData);
        assertionSteps.checkedStatusResponse(response, SC_OK);
        assertionSteps.checkedBodySuccessfulResponseChangeInformation(response, newEmail, fakeUser.getName());
    }

    @Test
    @DisplayName("Можно изменить name авторизованному пользователю")
    @Description("Ожидаем, что name будет изменён, код ответа: 200 ОК, тело ответа соответствует структуре: {\"success\":true, \"user\": {\"email\":\"\", \"name\":\"\" } }")
    public void changeNameTest() {
        String newName = fakerUtility.getFirstName();
        User changeData = User.builder()
                .name(newName)
                .build();
        Response response = userApi.setChangeInformationUser(accessToken, changeData);
        assertionSteps.checkedStatusResponse(response, SC_OK);
        assertionSteps.checkedBodySuccessfulResponseChangeInformation(response, fakeUser.getEmail(), newName);
    }

    @Test
    @DisplayName("Нельзя изменить информацию для неавторизованного пользователя")
    @Description("Ожидаем, что информация не будет изменена, код ответ: 401 Unauthorized, тело ответа: {\"success\":false,\"message\":\"You should be authorised\"}")
    public void mustNotChangeInformationUnauthorizedUser() {
        String newEmail = fakerUtility.getEmailAddress();
        User changeData = User.builder()
                .email(newEmail)
                .build();
        Response response = userApi.setChangeInformationUser(changeData);
        assertionSteps.checkedStatusResponse(response, SC_UNAUTHORIZED);
        assertionSteps.checkedBodyInvalidResponse(response, false, "You should be authorised");
    }

    @Test
    @DisplayName("Нельзя передать в запросе email, который уже используется")
    @Description("Ожидаем код ответ: 403 Forbidden, тело ответа: {\"success\":false,\"message\":\"User with such email already exists\"}")
    public void mustNotChangeEmailToIdentical() {
        String newEmail = fakerUtility.getEmailAddress();
        User newUser = new User(newEmail, fakeUser.getPassword(), fakeUser.getName());
        Response createNewUserResponse = userApi.createUser(newUser);
        String accessTokenNewUser = userApi.getAccessToken(createNewUserResponse);

        User changeData = User.builder()
                .email(newEmail)
                .build();
        Response response = userApi.setChangeInformationUser(accessToken, changeData);

        userApi.deleteUser(accessTokenNewUser);

        assertionSteps.checkedStatusResponse(response, SC_FORBIDDEN);
        assertionSteps.checkedBodyInvalidResponse(response, false, "User with such email already exists");
    }

    @After
    public void deleteUser() {
        userApi.deleteUser(accessToken);
    }
}
