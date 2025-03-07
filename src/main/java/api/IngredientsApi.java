package api;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import static api.ProjectURL.URL;
import static io.restassured.RestAssured.given;

public class IngredientsApi {
    private static final String INGREDIENTS_REQUEST = "/api/ingredients";

    @Step("Получение id ингредиента")
    public String getIdIngredient(String nameIngredient) {
        RestAssured.baseURI = URL;
        JsonPath jsonPath = given().header("Content-type", "application/json").get(INGREDIENTS_REQUEST).jsonPath();
        return jsonPath.getString(String.format("data.find {it.name == \"%s\"}._id", nameIngredient));
    }

}
