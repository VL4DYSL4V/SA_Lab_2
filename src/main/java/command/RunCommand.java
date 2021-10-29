package command;

import framework.command.AbstractRunnableCommand;

public class RunCommand extends AbstractRunnableCommand {

    private static final String NAME = "run";

    public RunCommand() {
        super(NAME);
    }

    @Override
    public void execute(String[] strings) {

    }
}
