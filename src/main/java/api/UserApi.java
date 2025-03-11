package api;

import constants.Endpoints;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class UserApi {

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return given().header("Content-type", "application/json").and().body(user).when().post(Endpoints.CREATE_USER_REQUEST.toString());
    }

    @Step("Авторизация пользователя")
    public Response loginUser(User user) {
        return given().header("Content-type", "application/json").and().body(user).when().post(Endpoints.LOGIN_USER_REQUEST.toString());
    }

    @Step("Получение accessToken")
    public String getAccessToken(Response response) {
        return response.jsonPath().getString("accessToken");
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken) {
        given().header("Authorization", accessToken).header("Content-type", "application/json").delete(Endpoints.DELETE_USER_REQUEST.toString()).then().statusCode(SC_ACCEPTED);
    }

    @Step("Изменение информации пользователя")
    public Response setChangeInformationUser(String accessToken, User changeDataUser) {
        if (accessToken != null) {
            return given().header("Authorization", accessToken).header("Content-type", "application/json").and().body(changeDataUser).patch(Endpoints.CHANGE_INFORMATION_USER_REQUEST.toString());
        } else {
            return given().header("Content-type", "application/json").and().body(changeDataUser).patch(Endpoints.CHANGE_INFORMATION_USER_REQUEST.toString());
        }
    }
}
