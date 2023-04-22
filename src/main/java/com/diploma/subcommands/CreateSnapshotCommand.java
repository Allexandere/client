package com.diploma.subcommands;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.diploma.model.Session;
import com.diploma.model.Snapshot;
import com.diploma.model.dto.SnapshotDto;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import static picocli.CommandLine.*;

@Command(
        name = "create-snapshot"
)
public class CreateSnapshotCommand implements Runnable {

    @Parameters
    private String sessionId;

    @Option(names = {"-t", "--title"})
    private String title;

    @Option(names = {"-d", "--description"})
    private String description;

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
        if (sessionId != null) {
            try {
                HttpResponse<Snapshot> snapshotHttpResponse = Unirest
                        .post("http://24manager.ru/snapshot")
                        .header("Content-Type", "application/json")
                        .body(getBody())
                        .asObject(Snapshot.class);
                HttpResponse<Session> sessionHttpResponse = Unirest
                        .get("http://24manager.ru/session")
                        .header("Content-Type", "application/json")
                        .queryString("sessionId", UUID.fromString(sessionId))
                        .asObject(Session.class);
                if (snapshotHttpResponse.getStatus() == 200 && sessionHttpResponse.getStatus() == 200) {
                    Snapshot snapshot = snapshotHttpResponse.getBody();
                    Session session = sessionHttpResponse.getBody();
                    System.out.println("Remote folder created. Starting file uploading. Snapshot ID: " + snapshot.getId());
                    File baseFolder = new File(session.getAbsolutePath());
                    File[] files = baseFolder.listFiles();
                    String currentRelativeS3Path = "";
                    for (File file : files) {
                        //uploadFiles(snapshot.getRelativeS3Path(), currentRelativeS3Path, file);
                        if (file.isDirectory()) {
                            System.out.println(file.getName() + " is directory, skipping");
                            continue;
                        }
                        if (file.isFile()) {
                            System.out.println("uploading " + file.getName());
                            PutObjectRequest build = new PutObjectRequest(BASE_BUCKET, snapshot.getRelativeS3Path() + file.getName(), file);
                            s3.putObject(build);
                        }
                    }
                    System.out.println("Finished");
                } else {
                    System.out.println("Something went wrong. Http code: " + snapshotHttpResponse.getStatus());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid session id");
            }
        } else {
            System.out.println("Specify the session id");
        }
    }

    /*
    private void uploadFiles(String absoluteS3Path, String currentRelativeS3Path, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File childFile : files) {
                uploadFiles(absoluteS3Path, currentRelativeS3Path + file.getName() + "/", childFile);
            }
            client.putObject()
            return;
        }

    }

     */

    private SnapshotDto getBody() {
        return SnapshotDto.builder()
                .sessionId(UUID.fromString(sessionId))
                .title(title)
                .description(description)
                .build();
    }
}
