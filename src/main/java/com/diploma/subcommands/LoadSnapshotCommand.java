package com.diploma.subcommands;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.diploma.model.Session;
import com.diploma.model.Snapshot;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

@Command(
        name = "load"
)
public class LoadSnapshotCommand implements Runnable {
    @Parameters
    private String sessionId;
    @Parameters
    private String snapshotId;

    private static final String BASE_BUCKET = "24211693-897d379b-b4c3-4a54-9db8-2e29aeb00169";

    private static final AWSCredentials credentials = new BasicAWSCredentials(
            "ce08849",
            "90cc11643873339a73aa22d8b555a52a"
    );

    private static final AmazonS3 s3 = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            "https://s3.timeweb.com", "ru-1"
                    )).build();

    @Override
    @SneakyThrows
    public void run() {
        if (snapshotId != null && sessionId != null) {
            try {
                HttpResponse<Snapshot> snapshotHttpResponse = Unirest
                        .get("http://24manager.ru/snapshot")
                        .header("Content-Type", "application/json")
                        .queryString("snapshotId", UUID.fromString(snapshotId))
                        .asObject(Snapshot.class);
                HttpResponse<Session> sessionHttpResponse = Unirest
                        .get("http://24manager.ru/session")
                        .header("Content-Type", "application/json")
                        .queryString("sessionId", UUID.fromString(sessionId))
                        .asObject(Session.class);

                if (snapshotHttpResponse.getStatus() == 200 && sessionHttpResponse.getStatus() == 200) {
                    Snapshot snapshot = snapshotHttpResponse.getBody();
                    Session session = sessionHttpResponse.getBody();
                    System.out.println("Downloading in " + session.getAbsolutePath());
                    FileUtils.cleanDirectory(new File(session.getAbsolutePath()));
                    List<S3ObjectSummary> fileListing = s3.listObjects(BASE_BUCKET, snapshot.getRelativeS3Path()).getObjectSummaries();
                    for (S3ObjectSummary fileSummary : fileListing) {
                        String fileName = StringUtils.removeStart(fileSummary.getKey(), snapshot.getRelativeS3Path());
                        if (fileName.isEmpty()) {
                            continue;
                        }
                        System.out.println("Downloading " + fileName);
                        GetObjectRequest objectRequest = new GetObjectRequest(BASE_BUCKET, fileSummary.getKey());
                        File file = new File(session.getAbsolutePath() + "\\" + fileName);
                        s3.getObject(objectRequest, file);
                    }
                    System.out.println("Finished");
                } else {
                    System.out.println("Something went wrong");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid session id or snapshot id");
            }
        } else {
            System.out.println("Specify the snapshot id or the session id");
        }
    }
}
