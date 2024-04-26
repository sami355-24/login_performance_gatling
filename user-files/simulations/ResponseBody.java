import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.OpenInjectionStep.atOnceUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.Random;

public class ResponseBody extends Simulation {

    public ResponseBody() {

        HttpProtocolBuilder httpProtocol = http
                .baseUrl("https://jsonplaceholder.typicode.com");

        ScenarioBuilder scn = scenario("Display Full HTTP Response Body")
                .exec(http("GET Request")
                        .get("/posts/1")
                        .check(status().is(200))
                        .check(bodyString().saveAs("responseBody")))
                .exec(session -> {
                    System.out.println("Response Body:");
                    System.out.println(session.getString("responseBody"));
                    return session;
                });

        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }


}
