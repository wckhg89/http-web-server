package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.util.Map;

/**
 * Created by kanghonggu on 2017. 5. 15..
 */
public class HttpRequest {

    private static Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private BufferedReader bufferedReader;
    private String requestLine;
    private Map<String , String> headerMap;


    public HttpRequest(InputStream inputStream) {
        try {
            this.bufferedReader = new BufferedReader
                    (new InputStreamReader(inputStream, "UTF-8"));

            this.requestLine = getRequestLine();

        } catch (Exception e) {
            logger.error("Error - {}", e);
        }
    }


    private String getRequestLine () throws IOException {
        String line = bufferedReader.readLine();

        if (line == null) {
            return null;
        }

        logger.info("HTTP STATUS - {}", line);

        return line;
    }

    public String getMethod () throws Exception {
        if (requestLine == null) {
            return null;
        }


        return HttpRequestUtils
                .parseRequestLine(requestLine, HttpRequestUtils.STATUS_METHOD);



    }

    public String getPath () {
        if (requestLine == null) {
            return null;
        }


        return HttpRequestUtils
                .parseRequestLine(requestLine, HttpRequestUtils.STATUS_PATH);
    }

    public String getProtocol () {
        if (requestLine == null) {
            return null;
        }


        return HttpRequestUtils
                .parseRequestLine(requestLine, HttpRequestUtils.STATUS_PROTOCOL);
    }

    public String getHeader (String key) {
        return headerMap.get(key);
    }

    public void getParameter () {

    }

    private void parseHeader(BufferedReader br, String line) throws IOException {
        while (!"".equals(line)) {
            line = br.readLine();
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);


            if (pair != null) {
                headerMap.put(pair.getKey(), pair.getValue());
                logger.info("Pair - {} ", pair.toString());
            }
        }
    }

}
