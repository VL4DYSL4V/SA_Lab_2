package main;

import command.RunCommand;
import framework.application.Application;
import framework.state.ApplicationState;
import state.LaboratoryState;

public class Main {

    private static final String PROPERTIES_PATH_STRING = "/laboratory.properties";

    public static void main(String[] args) {
        ApplicationState state = new LaboratoryState();
        Application application = new Application.ApplicationBuilder(PROPERTIES_PATH_STRING, state)
                .addCommand("run", new RunCommand())
                .build();
        application.start();
    }

}
