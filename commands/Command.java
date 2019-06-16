package commands;

public interface Command {
	public void run();
	public static Command emptyCommand = () -> {};
}
