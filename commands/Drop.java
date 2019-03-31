package commands;

import java.io.File;

import cmd.Main;
import schema.Schema;
import utils.FilesUtils;

public class Drop implements Command {
	final String tableName;
	final boolean ie; //if exists
	
	public Drop(String tableName, boolean ie) {
		this.tableName = tableName;
		this.ie = ie;
	}
	
	public void run() {
		if (removeSchema(tableName, ie))
			removeFiles(tableName);
	}
	
	/**
	 * @return if success
	 */
	private boolean removeSchema(String tableName, boolean ie) {
		if (!Schema.HaveSchema(tableName)) {
			if (!ie)
				throw new RuntimeException("you tried to drop a non existing table without the IF EXISTS");
			return false;
		}
		Schema.RemoveSchema(tableName);
		return true;
	}
	
	private void removeFiles(String tableName) {
		File file = new File(Main.rootdir + "\\" + tableName);
		FilesUtils.clearFolder(file);
		file.delete();
	}
}