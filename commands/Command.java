package commands;

public interface Command {
	void run();
	Command emptyCommand = () -> {};
}
