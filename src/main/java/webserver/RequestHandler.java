package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private final MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    String htmlType = "text/html";
    String cssType = "text/css";

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String startLine = br.readLine();
            System.out.println("startLine = " + startLine);
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

                if (line.startsWith("Cookie")) {
                    if (line.split(": ")[1].equals("logined=true")) {
                        logined = true;
                    }
                }
            }

            byte[] body = new byte[0];

            // 요구사항 1.1 - index.html 반환하기
            if (url.equals("/") || url.equals("/index.html") && method.equals("GET")) {
                body = Files.readAllBytes(Paths.get("./webapp" + "/index.html"));
            }

            // 요구사항 1.2 - GET 방식으로 회원가입하기
            if (method.equals("GET") && url.equals("/user/form.html")) {
                body = Files.readAllBytes(Paths.get("./webapp" + url));
            }

            if (method.equals("GET") && url.startsWith("/user/signup")) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(url.split("\\?")[1]);
                User newUser = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                memoryUserRepository.addUser(newUser);

                response302Header(dos, "/");
                return;
            }

            // 요구사항 1.3 - POST 방식으로 회원가입하기
            if (method.equals("POST") && url.startsWith("/user/signup")) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, requestContentLength));
                User newUser = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                memoryUserRepository.addUser(newUser);

                response302Header(dos, "/");
                return;
            }

            // 요구사항 1.5 - 로그인하기
            if (method.equals("GET") && url.equals("/user/login.html")) {
                body = Files.readAllBytes(Paths.get("./webapp" + url));
            }

            if (method.equals("GET") && url.equals("/user/login_failed.html")) {
                body = Files.readAllBytes(Paths.get("./webapp" + url));
            }

            if (method.equals("POST") && url.startsWith("/user/login")) {
                Map<String, String> queryParameter = HttpRequestUtils.parseQueryParameter(IOUtils.readData(br, requestContentLength));
                User findUser = memoryUserRepository.findUserById(queryParameter.get("userId"));
                if (findUser != null && findUser.getPassword().equals(queryParameter.get("password"))) {
                    response302HeaderWithCookie(dos, "/", true);
                } else {
                    response302HeaderWithCookie(dos, "/user/login_failed.html", false);
                }
                return;
            }

            // 요구사항 1.6 - 사용자 목록 출력
            if (method.equals("GET") && url.equals("/user/list.html")) {
                body = Files.readAllBytes(Paths.get("./webapp" + url));
            }

            if (method.equals("GET") && url.startsWith("/user/userList")) {
                System.out.println("logined = " + logined);
                if (logined) {
                    response302Header(dos, "/user/list.html");
                } else {
                    response302Header(dos, "/user/login.html");
                }
                return;
            }

            if (method.equals("GET") && url.endsWith(".css")) {
                body = Files.readAllBytes(Paths.get("./webapp" + url));
                response200Header(dos, body.length, cssType);
                responseBody(dos, body);
                return;
            }

            response200Header(dos, body.length, htmlType);
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