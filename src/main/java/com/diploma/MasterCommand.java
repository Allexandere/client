package com.diploma;


import com.diploma.service.MapperImpl;
import com.diploma.subcommands.*;
import com.mashape.unirest.http.Unirest;
import picocli.CommandLine;

import static picocli.CommandLine.*;

@Command(
        subcommands = {
                InitSessionCommand.class,
                CreateSnapshotCommand.class,
                ListSessionsCommand.class,
                ListSnapshotsCommand.class,
                LoadSnapshotCommand.class
        }
)
public class MasterCommand {

    public static void main(String[] args) {
        Unirest.setObjectMapper(new MapperImpl());
        CommandLine commandLine = new CommandLine(new MasterCommand());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
