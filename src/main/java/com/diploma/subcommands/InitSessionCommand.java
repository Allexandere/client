package com.diploma.subcommands;


import com.diploma.model.Session;
import com.diploma.model.dto.SessionDto;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static picocli.CommandLine.*;

@Command(
        name = "init"
)
public class InitSessionCommand implements Runnable {

    @Parameters
    private Path path;

    @Option(names = {"-t", "--title"})
    private String title;

    @Option(names = {"-d", "--description"})
    private String description;

    //@Option(names = {"-p", "--parent"})
    //private String parentSessionId;

    @Override
    @SneakyThrows
    public void run() {
        if (path != null) {
            if (Files.isDirectory(path)) {
                HttpResponse<Session> sessionHttpResponse = Unirest
                        .post("http://24manager.ru/session")
                        .header("Content-Type", "application/json")
                        .body(getSessionBody())
                        .asObject(Session.class);
                if (sessionHttpResponse.getStatus() == 200) {
                    System.out.println("Session created. Session: " + sessionHttpResponse.getBody().toString());
                } else {
                    System.out.println("Something went wrong. Http code: " + sessionHttpResponse.getStatus());
                }
            } else {
                System.out.println("Invalid directory path");
            }
        } else {
            System.out.println("Specify the directory path");
        }
    }

    private SessionDto getSessionBody() {
        String ownerId = System.getenv("USER_ID");
        return SessionDto.builder()
                .title(title)
                .description(description)
                .absolutePath(path.toString())
                .ownerId(UUID.fromString(ownerId))
                .build();
    }
}
