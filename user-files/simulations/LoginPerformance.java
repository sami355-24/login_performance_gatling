import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.incrementUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LoginPerformance extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8081")
            .contentTypeHeader("application/json");

    private static final Random random = new Random();

    private static final Iterator<Map<String, Object>> randomFeeder = Stream.generate(
            (Supplier<Map<String, Object>>) () -> Map.of("randomNumber", random.nextInt(50) + 1)).iterator();


    private final ChainBuilder loginScenario =
            feed(randomFeeder)
                    .exec(login("1-a. 로그인"));

    private final ChainBuilder simpleRequestScenario =
            feed(randomFeeder)
                    .exec(login("1-a. 로그인"))
                    .exec(fakeBusinessLogic("1-b. 비즈니스 로직"));

    private final ChainBuilder loadRequestScenario =
            feed(randomFeeder)
                    .exec(login("1-a. 로그인"))
                    .exec(fakeBusinessLogic("1-b. 비즈니스 로직"))
                    .exec(realBusinessLogic("1-c. 비즈니스 로직"));



    private static HttpRequestActionBuilder login(String name) {
        return http(name)
                .post("/auth/login")
                .body(StringBody("""
                                                  {
                                                  "email": "user#{randomNumber}@test.com",
                                                  "password": "1234"
                                                  }
                        """))
                .check(status().is(200))
                .check(jsonPath("$.accessToken").saveAs("authToken"));
    }

    private static HttpRequestActionBuilder fakeBusinessLogic(String name) {
        return http(name)
                .get("/load/fake")
                .header("Authorization", "Bearer #{authToken}")
                .check(status().is(200));
    }

    private static HttpRequestActionBuilder realBusinessLogic(String name) {
        return http(name)
                .get("/load/real")
                .check(status().is(200));
    }

    private final ScenarioBuilder scenario1 = scenario("1번 시나리오 유저").exec(loginScenario);
    private final ScenarioBuilder scenario2 = scenario("2번 시나리오 유저").exec(simpleRequestScenario);
    private final ScenarioBuilder scenario3 = scenario("3번 시나리오 유저").exec(loadRequestScenario);
    {
        setUp(
//                scenario1.injectOpen(
//                        incrementUsersPerSec(50)
//                                .times(30) // 테스트가 끝날 때까지 사용자 수를 지속적으로 증가시킴
//                                .eachLevelLasting(1) // 각 사용자 수를 1초 동안 유지
//                                .startingFrom(1) // 사용자 수를 1명부터 시작
//                )
//
                scenario2.injectOpen(
                        incrementUsersPerSec(45)
                                .times(30) // 테스트가 끝날 때까지 사용자 수를 지속적으로 증가시킴
                                .eachLevelLasting(1) // 각 사용자 수를 1초 동안 유지
                                .startingFrom(1) // 사용자 수를 1명부터 시작
                )

//                scenario3.injectOpen(
//                        incrementUsersPerSec(1)
//                                .times(30) // 테스트가 끝날 때까지 사용자 수를 지속적으로 증가시킴
//                                .eachLevelLasting(1) // 각 사용자 수를 1초 동안 유지
//                                .startingFrom(1) // 사용자 수를 1명부터 시작
//                )
        ).protocols(httpProtocol);
    }


}
