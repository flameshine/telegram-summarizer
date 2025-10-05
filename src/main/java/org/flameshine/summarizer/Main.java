package org.flameshine.summarizer;

import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        var exit = new CommandLine(new AnalyzeCommand()).execute(args);
        System.exit(exit);
    }
}