package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import model.User;
import org.junit.Test;

import util.HttpRequestUtils.Pair;

public class HttpRequestUtilsTest {

    @Test
    public void PATH에서_쿼리스트링만_추출하기_테스트 () {
        String requestLine = "GET /user/create?userId=admin&password=test&name=%EA%B0%95%ED%99%8D%EA%B5%AC&email=wckhg89%40naver.com HTTP/1.1";

        String path = HttpRequestUtils.parseRequestLine(requestLine, HttpRequestUtils.STATUS_PATH);

        String queryString = HttpRequestUtils.getPathToQueryString(path);

        assertThat(queryString, is("userId=admin&password=test&name=%EA%B0%95%ED%99%8D%EA%B5%AC&email=wckhg89%40naver.com"));


    }

    @Test
    public void STATUS_LINE_해석_테스트 () {
        String requestLine = "GET /index.html HTTP/1.1";

        String method = HttpRequestUtils.parseRequestLine(requestLine, HttpRequestUtils.STATUS_METHOD);
        assertThat(method, is("GET"));

        String path = HttpRequestUtils.parseRequestLine(requestLine, HttpRequestUtils.STATUS_PATH);
        assertThat(path, is("/index.html"));

        String protocol = HttpRequestUtils.parseRequestLine(requestLine, HttpRequestUtils.STATUS_PROTOCOL);
        assertThat(protocol, is("HTTP/1.1"));

        String notValidMinus = HttpRequestUtils.parseRequestLine(requestLine, -1);
        assertThat(notValidMinus, is(""));

        String notValidPlus = HttpRequestUtils.parseRequestLine(requestLine, 4);
        assertThat(notValidPlus, is(""));
    }

    @Test
    public void parseQueryString_PATH_이용한_모델객체_만들기_테스 () {
        User mockUser = new User("admin","test","강홍구", "wckhg89@naver.com");
        String requestLine = "GET /user/create?userId=admin&password=test&name=강홍구&email=wckhg89@naver.com HTTP/1.1";

        String path = HttpRequestUtils.parseRequestLine(requestLine, HttpRequestUtils.STATUS_PATH);

        String queryString = HttpRequestUtils.getPathToQueryString(path);
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);

        String userId = parameters.get("userId");
        String password = parameters.get("password");
        String name = parameters.get("name");
        String email = parameters.get("email");

        User realUser = new User(userId, password, name, email);


        assertEquals(mockUser.toString(), realUser.toString());
    }



    @Test
    public void parseQueryString() {
        String queryString = "userId=javajigi";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));

        queryString = "userId=javajigi&password=password2";
        parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is("password2"));


    }

    @Test
    public void parseQueryString_null() {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(null);
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString("");
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString(" ");
        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    public void parseQueryString_invalid() {
        String queryString = "userId=javajigi&password";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));
    }

    @Test
    public void parseCookies() {
        String cookies = "logined=true; JSessionId=1234";
        Map<String, String> parameters = HttpRequestUtils.parseCookies(cookies);
        assertThat(parameters.get("logined"), is("true"));
        assertThat(parameters.get("JSessionId"), is("1234"));
        assertThat(parameters.get("session"), is(nullValue()));
    }

    @Test
    public void getKeyValue() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId=javajigi", "=");
        assertThat(pair, is(new Pair("userId", "javajigi")));
    }

    @Test
    public void getKeyValue_invalid() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId", "=");
        assertThat(pair, is(nullValue()));
    }

    @Test
    public void parseHeader() throws Exception {
        String header = "Content-Length: 59";
        Pair pair = HttpRequestUtils.parseHeader(header);
        assertThat(pair, is(new Pair("Content-Length", "59")));
    }

    @Test
    public void PairMap_만들기_테스트 () {

    }
}
