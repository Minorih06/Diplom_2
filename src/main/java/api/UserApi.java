package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class UserApi {
    private static final String CREATE_USER = "/api/auth/register";
    private static final String LOGIN_USER = "/api/auth/login";
    private static final String DELETE_USER = "/api/auth/user";
    private static final String CHANGE_INFORMATION_USER = "/api/auth/user";

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return given().header("Content-type", "application/json").and().body(user).when().post(CREATE_USER);
    }

    @Step("Авторизация пользователя")
    public Response loginUser(User user) {
        return given().header("Content-type", "application/json").and().body(user).when().post(LOGIN_USER);
    }

    @Step("Получение accessToken")
    public String getAccessToken(Response response) {
        return response.jsonPath().getString("accessToken");
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken) {
        given().header("Authorization", accessToken).header("Content-type", "application/json").delete(DELETE_USER).then().statusCode(SC_ACCEPTED);
    }

    @Step("Изменение информации пользователя")
    public Response setChangeInformationUser(String accessToken, User changeDataUser) {
        if (accessToken != null) {
            return given().header("Authorization", accessToken).header("Content-type", "application/json").and().body(changeDataUser).patch(CHANGE_INFORMATION_USER);
        } else {
            return given().header("Content-type", "application/json").and().body(changeDataUser).patch(CHANGE_INFORMATION_USER);
        }
    }
}
