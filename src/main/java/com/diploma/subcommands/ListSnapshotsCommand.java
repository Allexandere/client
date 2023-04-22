package com.diploma.subcommands;


import com.diploma.model.Session;
import com.diploma.model.Snapshot;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Command;

@Command(
        name = "snapshots"
)
public class ListSnapshotsCommand implements Runnable {

    @Parameters
    private String sessionId;

    @Override
    @SneakyThrows
    public void run() {
        if (sessionId != null) {
            try {
                HttpResponse<Snapshot[]> sessionHttpResponse = Unirest
                        .get("http://24manager.ru/snapshots")
                        .header("Content-Type", "application/json")
                        .queryString("sessionId", UUID.fromString(sessionId))
                        .asObject(Snapshot[].class);
                if (sessionHttpResponse.getStatus() == 200) {
                    List<Snapshot> snapshots = Arrays.asList(sessionHttpResponse.getBody());
                    System.out.println(snapshots);
                } else {
                    System.out.println("Something went wrong");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid session id");
            }
        } else {
            System.out.println("Specify the session id");
        }
    }
}

