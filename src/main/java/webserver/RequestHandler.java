package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = getRequestLine(br);

            if (line == null) return;

            String method
                    = HttpRequestUtils.parseRequestLine(line, HttpRequestUtils.STATUS_METHOD);

            if ("GET".equals(method)) {
                processGet(out, br, line);
            }

            if ("POST".equals(method)) {
                processPost(out, br, line);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getRequestLine(BufferedReader br) throws IOException {
        String line = br.readLine();

        if (line == null) {
            return null;
        }

        log.info("HTTP STATUS - {}", line);
        return line;
    }

    private void processPost (OutputStream out, BufferedReader br, String line) throws IOException {
        String path = HttpRequestUtils.parseRequestLine(line, HttpRequestUtils.STATUS_PATH);
        log.info("PATH - {}", path);

        Map<String , HttpRequestUtils.Pair> pairMap = Maps.newHashMap();
        parseHeader(br, line, pairMap);

        String data
                = IOUtils
                .readData(br, Integer.parseInt(pairMap.get("Content-Length").getValue()));
        log.debug("DATA - {}", data);

        Map<String, String> parameters = HttpRequestUtils.parseQueryString(data);
        DataOutputStream dos = new DataOutputStream(out);

        if (path.contains("user/create")) {

            String userId = parameters.get("userId");
            String password = parameters.get("password");
            String name = parameters.get("name");
            String email = parameters.get("email");

            User user = new User(userId, password, name, email);

            log.debug("User - {}", user.toString());

            DataBase.addUser(user);


            response302Header(dos, pairMap.get("Accept").getValue(),"http://localhost:8080/index.html");

            return;
        }

        if (path.contains("user/login")) {
            String userId = parameters.get("userId");

            User loginUser = DataBase.findUserById(userId);

            if (loginUser != null) {
                response302LoginedHeader(dos, pairMap.get("Accept").getValue(),"http://localhost:8080/index.html");

                return;
            }

            response302Header(dos, pairMap.get("Accept").getValue(),"http://localhost:8080/index.html");

        }

    }

    private void processGet(OutputStream out, BufferedReader br, String line) throws IOException {
        String path = HttpRequestUtils.parseRequestLine(line, HttpRequestUtils.STATUS_PATH);
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
