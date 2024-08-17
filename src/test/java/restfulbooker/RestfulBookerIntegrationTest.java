package restfulbooker;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RestfulBookerIntegrationTest {

    private static String authToken;
    private static String authTokenForPutDelete;
    private static int bookingId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        // Authenticate and get token for general use
        Response response = given()
                .contentType("application/json")
                .body("{\"username\": \"admin\", \"password\": \"password123\"}")
                .post("/auth");

        authToken = response.jsonPath().getString("token");

        // Authenticate and get token for PUT and DELETE requests
        Response responseForPutDelete = given()
                .contentType("application/json")
                .body("{\"username\": \"admin\", \"password\": \"password123\"}")
                .post("/auth");

        authTokenForPutDelete = responseForPutDelete.jsonPath().getString("token");
    }

    @Test
    public void testAuthenticateUserInvalidCredentials() {
        given()
                .contentType("application/json")
                .body("{\"username\": \"wrongadmin\", \"password\": \"wrongpassword\"}")
                .put("/auth")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateBooking() {
        Response response = given()
                .contentType("application/json")
                .header("Cookie", "token=" + authToken)
                .body("{\"firstname\": \"Jim\", \"lastname\": \"Brown\", \"totalprice\": 111, \"depositpaid\": true, \"bookingdates\": {\"checkin\": \"2023-01-01\", \"checkout\": \"2023-01-02\"}, \"additionalneeds\": \"Breakfast\"}")
                .post("/booking");

        response.then().statusCode(200);
        bookingId = response.jsonPath().getInt("bookingid");
    }

    @Test
    public void testGetBooking() {
        given()
                .pathParam("id", bookingId)
                .get("/booking/{id}")
                .then()
                .statusCode(200)
                .body("firstname", equalTo("Jim"))
                .body("lastname", equalTo("Brown"));
    }

    @Test
    public void testGetBookingNotFound() {
        given()
                .pathParam("id", 99999)
                .get("/booking/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpdateBooking() {
        given()
                .contentType("application/json")
                .header("Cookie", "token=" + authTokenForPutDelete)
                .pathParam("id", bookingId)
                .body("{\"firstname\": \"James\", \"lastname\": \"Brown\", \"totalprice\": 111, \"depositpaid\": true, \"bookingdates\": {\"checkin\": \"2018-01-01\", \"checkout\": \"2019-01-01\"}, \"additionalneeds\": \"Breakfast\"}")
                .put("/booking/{id}")
                .then()
                .statusCode(200)
                .body("firstname", equalTo("James"));
    }

    @Test
    public void testDeleteBooking() {
        given()
                .header("Cookie", "token=" + authTokenForPutDelete)
                .pathParam("id", bookingId)
                .delete("/booking/{id}")
                .then()
                .statusCode(201);
    }

    @Test
    public void testCreateBookingInvalidData() {
        given()
                .body("{ \"invalid\": \"data\" }")
                .when()
                .post("/booking")
                .then()
                .statusCode(500);
    }

    @Test
    public void testDeleteBookingNotFound() {
        given()
                .header("Cookie", "token=" + authTokenForPutDelete)
                .when()
                .delete("/booking/9999")
                .then()
                .statusCode(405);
    }

    @Test
    public void testAuthenticateUser() {
        given()
                .body("{ \"username\": \"admin\", \"password\": \"password123\" }")
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .body("token", not(empty()));
    }

    @Test
    public void testUpdateBookingInvalidData() {
        given()
                .header("Cookie", "token=" + authTokenForPutDelete)
                .body("{ \"invalid\": \"data\" }")
                .when()
                .put("/booking/1")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateBookingMissingFields() {
        given()
                .header("Cookie", "token=" + authToken)
                .body("{\"firstname\": \"Jim\"}")
                .when()
                .post("/booking")
                .then()
                .statusCode(500);
    }

    @Test
    public void testUpdateBookingMissingFields() {
        given()
                .header("Cookie", "token=" + authTokenForPutDelete)
                .body("{\"firstname\": \"James\"}")
                .when()
                .put("/booking/1")
                .then()
                .statusCode(400);
    }

    @Test
    public void testDeleteBookingWithoutAuth() {
        given()
                .pathParam("id", bookingId)
                .when()
                .delete("/booking/{id}")
                .then()
                .statusCode(403);
    }

    @Test
    public void testGetBookingInvalidIdFormat() {
        given()
                .pathParam("id", "invalid")
                .when()
                .get("/booking/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testAuthenticateUserMissingFields() {
        given()
                .body("{\"username\": \"admin\"}")
                .when()
                .post("/auth")
                .then()
                .statusCode(200);
    }
}