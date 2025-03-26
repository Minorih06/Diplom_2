package api;

import constants.Endpoints;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.given;

public class IngredientsApi {

    @Step("Получение id ингредиента")
    public String getIdIngredient(String nameIngredient) {
        JsonPath jsonPath = given().header("Content-type", "application/json").get(Endpoints.INGREDIENTS_REQUEST.getUrl()).jsonPath();
        return jsonPath.getString(String.format("data.find {it.name == \"%s\"}._id", nameIngredient));
    }

}
