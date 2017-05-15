package webserver;

import com.google.common.collect.Maps;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

/**
 * Created by kanghonggu on 2017. 5. 15..
 */
public class HttpRequest {

    private static Logger log = LoggerFactory.getLogger(HttpRequest.class);

    public void get(OutputStream out, BufferedReader br, String line) throws IOException {
        String path =
                HttpRequestUtils
                .parseRequestLine(line, HttpRequestUtils.STATUS_PATH);
        log.info("PATH - {}", path);

        String queryString = HttpRequestUtils.getPathToQueryString(path);
        User user = null;

        if (!"".equals(queryString)) {
            Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);

            String userId = parameters.get("userId");
            String password = parameters.get("password");
            String name = parameters.get("name");
            String email = parameters.get("email");

            user = new User(userId, password, name, email);
            log.info("User - {} ", user.toString());
        }


        Map<String , HttpRequestUtils.Pair> pairMap = Maps.newHashMap();
        parseHeader(br, line, pairMap);

        DataOutputStream dos = new DataOutputStream(out);

        if (path.contains("user/list")) {
            try {
                String cookie = pairMap.get("Cookie").getValue();

                if (!"logined=true".equals(cookie)) {
                    response302Header(dos, pairMap.get("Accept").getValue(),"http://localhost:8080/index.html");
                }

            } catch (Exception e) {
                response302Header(dos, pairMap.get("Accept").getValue(),"http://localhost:8080/index.html");
            }

        }


        byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
        response200Header(dos, pairMap.get("Accept").getValue(), body.length);
        responseBody(dos, body);
    }

    private void parseHeader(BufferedReader br, String line, Map<String, HttpRequestUtils.Pair> pairMap) throws IOException {
        while (!"".equals(line)) {
            line = br.readLine();
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);


            if (pair != null) {
                pairMap.put(pair.getKey(), pair);
                log.info("Pair - {} ", pair.toString());
            }
        }
    }

    private void response302Header (DataOutputStream dos, String contentType, String location) throws IOException {
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Content-Type: " +  contentType+ ";charset=utf-8\r\n");
        dos.writeBytes("Location: " + location +" \r\n");

    }

    private void response302LoginedHeader (DataOutputStream dos, String contentType, String location) throws IOException {
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Content-Type: " +  contentType+ ";charset=utf-8\r\n");
        dos.writeBytes("Location: " + location + "\r\n");
        dos.writeBytes("Set-Cookie: logined=true");
        dos.writeBytes("\r\n");

    }

    private void response200Header(DataOutputStream dos, String contentType, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: "+ contentType + " \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
