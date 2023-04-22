package com.diploma.subcommands;


import com.diploma.model.Session;
import com.diploma.model.Snapshot;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

@Command(
        name = "sessions"
)
public class ListSessionsCommand implements Runnable {

    @Override
    @SneakyThrows
    public void run() {
        try {
            String ownerId = System.getenv("USER_ID");
            HttpResponse<Session[]> sessionHttpResponse = Unirest
                    .get("http://24manager.ru/sessions")
                    .header("Content-Type", "application/json")
                    .queryString("ownerId", UUID.fromString(ownerId))
                    .asObject(Session[].class);
            if (sessionHttpResponse.getStatus() == 200) {
                List<Session> sessions = Arrays.asList(sessionHttpResponse.getBody());
                System.out.println(sessions);
            }
            else {
                System.out.println("Something went wrong");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid owner id");
        }
    }
}

