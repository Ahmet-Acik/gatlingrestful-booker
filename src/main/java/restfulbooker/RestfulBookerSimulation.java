package restfulbooker;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

public class RestfulBookerSimulation extends Simulation {

    // HTTP Configuration
    private HttpProtocolBuilder httpProtocol = HttpDsl.http
            .baseUrl("https://restful-booker.herokuapp.com")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

    // Runtime Parameters
    private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "1"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("RAMP_DURATION", "10"));

    // Before Block
    public void before() {
        System.out.println("USERS: " + USER_COUNT);
        System.out.println("RAMPUP_DURATION: " + RAMP_DURATION);
    }

    // ChainBuilder for authenticating the user
    private static ChainBuilder authenticate =
            CoreDsl.exec(HttpDsl.http("Authenticate")
                    .post("/auth")
                    .body(CoreDsl.StringBody("{\"username\": \"admin\", \"password\": \"password123\"}")).asJson()
                    .check(CoreDsl.jsonPath("$.token").saveAs("authToken")));

    // ChainBuilder for creating a new booking
    private static ChainBuilder createBooking =
            CoreDsl.exec(HttpDsl.http("Create Booking")
                    .post("/booking")
//                    .header("Cookie", "token=${authToken}")
                    .body(CoreDsl.StringBody("{\"firstname\": \"Jim\", \"lastname\": \"Brown\", \"totalprice\": 111, \"depositpaid\": true, \"bookingdates\": {\"checkin\": \"2023-01-01\", \"checkout\": \"2023-01-02\"}, \"additionalneeds\": \"Breakfast\"}")).asJson()
                    .check(CoreDsl.jsonPath("$.bookingid").saveAs("bookingId")));

    // ChainBuilder for retrieving the booking
    private static ChainBuilder getBooking =
            CoreDsl.exec(HttpDsl.http("Get Booking")
                    .get("/booking/${bookingId}")
//                    .header("Cookie", "token=${authToken}")
                    .check(HttpDsl.status().is(200))
                    .check(CoreDsl.jsonPath("$.firstname").is("Jim"))
                    .check(CoreDsl.jsonPath("$.lastname").is("Brown")));

    // ChainBuilder for updating the booking
    private static ChainBuilder updateBooking =
            CoreDsl.exec(HttpDsl.http("Update Booking")
                    .put("/booking/${bookingId}")
                    .header("Cookie", "token=${authToken}")
                    .body(CoreDsl.StringBody("{\"firstname\": \"James\", \"lastname\": \"Brown\", \"totalprice\": 111, \"depositpaid\": true, \"bookingdates\": {\"checkin\": \"2018-01-01\", \"checkout\": \"2019-01-01\"}, \"additionalneeds\": \"Breakfast\"}")).asJson()
                    .check(HttpDsl.status().is(200))
                    .check(CoreDsl.jsonPath("$.firstname").is("James")));

    // ChainBuilder for deleting the booking
    private static ChainBuilder deleteBooking =
            CoreDsl.exec(HttpDsl.http("Delete Booking")
                    .delete("/booking/${bookingId}")
                    .header("Cookie", "token=${authToken}")
                    .check(HttpDsl.status().is(201)));

    // Scenario Definition
    private ScenarioBuilder scn = CoreDsl.scenario("RestfulBookerSimulation")
            .exec(authenticate)
            .pause(Duration.ofSeconds(1))
            .exec(createBooking)
            .pause(Duration.ofSeconds(1))
            .exec(getBooking)
            .pause(Duration.ofSeconds(1))
            .exec(updateBooking)
            .pause(Duration.ofSeconds(1))
            .exec(deleteBooking);

    // Setup the simulation
    {
        setUp(scn.injectOpen(
                        CoreDsl.nothingFor(5),
                        CoreDsl.rampUsers(USER_COUNT).during(RAMP_DURATION))
                .protocols(httpProtocol));
    }
}