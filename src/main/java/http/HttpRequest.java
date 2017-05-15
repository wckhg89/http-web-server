package http;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.Map;

/**
 * Created by kanghonggu on 2017. 5. 15..
 */
public class HttpRequest {

    private static Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private BufferedReader bufferedReader;

    private String method;
    private String path;
    private String protocol;

    private Map<String, String> parameterMap;
    private Map<String , String> headerMap;


    public HttpRequest(InputStream inputStream) {
        try {
            this.bufferedReader = new BufferedReader
                    (new InputStreamReader(inputStream, "UTF-8"));

            String requestLine = getRequestLine();

            this.setMethod(requestLine);
            this.setPath(requestLine);
            this.setProtocol(requestLine);
            this.setHeader(requestLine);
            this.setParameter();

        } catch (Exception e) {
            logger.error("Error - {}", e);
        }
    }


    public String getMethod () {
        return this.method;
    }

    public String getPath () {
        return this.path.split("\\?")[0];
    }

    public String getProtocol () {
        return this.protocol;
    }

    public String getHeader (String key) {
        return headerMap.get(key);
    }

    public String getParameter (String key) {
        return parameterMap.get(key);
    }

    private void setParameter () throws IOException {
        if (parameterMap == null) {
            parameterMap = Maps.newHashMap();
        }


        if ("GET".equals(this.method)) {
            setGetParameters();
            return;
        }

        if ("POST".equals(this.method)) {
            setPostParameters();

            return;
        }
    }

    private void setGetParameters() {
        String queryString = HttpRequestUtils.getPathToQueryString(path);

        this.parameterMap = HttpRequestUtils.parseQueryString(queryString);
    }

    private void setPostParameters() throws IOException {
        String data = IOUtils
                .readData(bufferedReader, Integer.parseInt(getHeader("Content-Length")));

        this.parameterMap = HttpRequestUtils.parseQueryString(data);
    }


    private String getRequestLine () throws IOException {
        String line = bufferedReader.readLine();

        if (line == null) {
            return null;
        }

        logger.info("HTTP STATUS - {}", line);

        return line;
    }

    private void setMethod(String requestLine) {
        if (requestLine == null) {
            this.method = null;
        }


        this.method = HttpRequestUtils
                .parseRequestLine(requestLine, HttpRequestUtils.STATUS_METHOD);

    }

    private void setPath(String requestLine) {
        if (requestLine == null) {
            this.path = null;
        }

        this.path = HttpRequestUtils
                .parseRequestLine(requestLine, HttpRequestUtils.STATUS_PATH);
    }

    private void setProtocol(String requestLine) {
        if (requestLine == null) {
            this.protocol = null;
        }


        this.protocol =  HttpRequestUtils
                .parseRequestLine(requestLine, HttpRequestUtils.STATUS_PROTOCOL);
    }

    private void setHeader(String requestLine) throws IOException {
        if (headerMap == null) {
            headerMap = Maps.newHashMap();
        }


        while (!"".equals(requestLine)) {
            requestLine = bufferedReader.readLine();
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(requestLine);


            if (pair != null) {
                headerMap.put(pair.getKey(), pair.getValue());
                logger.info("Pair - {} ", pair.toString());
            }
        }
    }

}
