package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private static final String ROOT_URL = "./webapp";
    private static final String HOME_URL = "/";
    private static final String HTML_TYPE = "text/html";
    private static final String CSS_TYPE = "text/css";
    private final Repository repository;

    public RequestHandler(Socket connection) {
        this.connection = connection;
        this.repository = MemoryUserRepository.getInstance();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            byte[] body = new byte[0];

            // Header 분석
            String startLine = br.readLine();
            String[] startLines = startLine.split(" ");
            String method = startLines[0];
            String url = startLines[1];

            int requestContentLength = 0;
            boolean logined = false;

            while (true) {
                final String line = br.readLine();
                if (line.equals("")) {
                    break;
                }

                // header info
                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                }

                if (line.startsWith("Cookie") && line.split(": ")[1].equals("logined=true")) {
                    logined = true;
                }
            }

            // 요구사항 1.1 - index.html 반환하기
            if (method.equals("GET") && url.endsWith(".html")) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + url));
            }

            if (method.equals("GET") && url.equals(HOME_URL)) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + "/index.html"));
            }

            // 요구사항 1.2 - GET 방식으로 회원가입하기
            if (method.equals("GET") && url.startsWith("/user/signup")) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(url.split("\\?")[1]);
                User newUser = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                repository.addUser(newUser);

                response302Header(dos, HOME_URL);
                return;
            }

            // 요구사항 1.3 - POST 방식으로 회원가입하기
            if (method.equals("POST") && url.startsWith("/user/signup")) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, requestContentLength));
                User newUser = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                repository.addUser(newUser);

                response302Header(dos, HOME_URL);
                return;
            }

            // 요구사항 1.5 - 로그인하기
            if (method.equals("POST") && url.startsWith("/user/login")) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, requestContentLength));
                User findUser = repository.findUserById(queryParameter.get("userId"));
                if (findUser != null && findUser.getPassword().equals(queryParameter.get("password"))) {
                    response302HeaderWithCookie(dos, HOME_URL, true);
                } else {
                    response302Header(dos, "/user/login_failed.html");
                }
                return;
            }

            // 요구사항 1.6 - 사용자 목록 출력
            if (method.equals("GET") && url.startsWith("/user/userList")) {
                if (!logined) {
                    response302Header(dos, "/user/login.html");
                    return;
                }
                body = Files.readAllBytes(Paths.get(ROOT_URL + "/user/list.html"));
            }

            // 요구사항 1.7 - CSS 출력
            if (method.equals("GET") && url.endsWith(".css")) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + url));
                response200Header(dos, body.length, CSS_TYPE);
                responseBody(dos, body);
                return;
            }

            response200Header(dos, body.length, HTML_TYPE);
            responseBody(dos, body);
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String path, Boolean logined) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + logined +  "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String type) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + type + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}