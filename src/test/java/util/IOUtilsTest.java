package util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

import model.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(IOUtilsTest.class);

    @Test
    public void readData() throws Exception {
        String data = "userId=admin&password=test&name=124&email=124%40124";
        StringReader sr = new StringReader(data);
        BufferedReader br = new BufferedReader(sr);

        logger.debug("parse body : {}", IOUtils.readData(br, data.length()));
    }


    @Test
    public void User_객체_만들기_테스트 () throws Exception {
        String data = "userId=admin&password=test&name=124&email=124%40124";
        StringReader sr = new StringReader(data);
        BufferedReader br = new BufferedReader(sr);

        String queryString = IOUtils.readData(br, data.length());

        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);

        logger.debug("parse body : {}", parameters.toString());

        String userId = parameters.get("userId");
        String password = parameters.get("password");
        String name = parameters.get("name");
        String email = parameters.get("email");

        User user = new User(userId, password, name, email);

        logger.debug("User - {} ", user.toString());
    }
}
