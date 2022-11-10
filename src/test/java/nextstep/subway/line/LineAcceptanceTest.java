package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.dto.LineRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("지하철 노선 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LineAcceptanceTest {
    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
        }
    }

    /**
     * When 지하철노선을 생성하면
     * Then 지하철노선이 생성된다
     * Then 지하철노선 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철노선을 생성한다.")
    @Test
    void createLine() {
        // when
        LineRequest lineRequest = LineRequest.builder()
                .name("2호선")
                .color("green")
                .upStationName("강남역")
                .downStationName("논현역")
                .build();
        ExtractableResponse<Response> response = createLine(lineRequest);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        List<Map> lines = getAllLine().jsonPath().get();
        assertAll(
                () -> assertThat(lines.stream().map(map -> map.get("name"))).containsAnyOf("2호선"),
                () -> assertThat(lines.stream().map(map -> map.get("color"))).containsAnyOf("green"));
    }

    /**
     * Given 2개의 지하철노선을 생성하고
     * When 지하철노선 목록을 조회하면
     * Then 2개의 지하철노선을 응답 받는다
     */
    @DisplayName("지하철노선을 조회한다.")
    @Test
    void getLines() {
        // when
        createLine(LineRequest.builder()
                .name("2호선")
                .color("green")
                .upStationName("강남역")
                .downStationName("논현역")
                .build());
        createLine(LineRequest.builder()
                .name("1호선")
                .color("blue")
                .upStationName("철산역")
                .downStationName("김포역")
                .build());

        // then
        List<Map> lines = getAllLine().jsonPath().get();
        assertAll(
                () -> assertThat(lines.stream().map(map -> map.get("name"))).containsAnyOf("2호선","1호선"),
                () -> assertThat(lines.stream().map(map -> map.get("color"))).containsAnyOf("green","blue"));

    }

    ExtractableResponse<Response> createLine(LineRequest lineRequest) {
        return RestAssured.given().log().all()
                .body(lineRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();
    }

    ExtractableResponse<Response> getAllLine() {
        return  RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .extract();
    }

}
