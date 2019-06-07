package commands;

import java.io.File;

import commandLine.Main;
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
		if (!Schema.HaveSchema(tableName)) {
			if (!ie)
				throw new RuntimeException("you tried to drop a non existing table without the IF EXISTS");
			return;
		}
		Schema.RemoveSchema(tableName);
		File tableFile = new File(Main.rootdir + "\\" + tableName);
		FilesUtils.clearFolder(tableFile);
		tableFile.delete();
	}
}