package commands;

import commandLine.Main;
import schema.Schema;
import utils.FilesUtils;

import java.io.File;

public class Drop implements Command {
	final String tableName;
	final boolean ie; //if exists

	public Drop(String tableName, boolean ie) {
		this.tableName = tableName;
		this.ie = ie;
	}

	public void run() {
		String tableDirName = String.join(File.separator, Main.rootdir, tableName);
		File tableFile = new File(tableDirName);
		if (tableFile.exists()) {
			FilesUtils.clearFolder(tableFile);
			tableFile.delete();
		}
		if (Schema.HaveSchema(tableName)) {
			Schema.RemoveSchema(tableName);
		} else {
			if (!ie) {
				throw new RuntimeException("you tried to drop a non existing table without the IF EXISTS");
			}
		}
	}

}