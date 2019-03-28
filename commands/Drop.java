package commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cmd.Main;
import schema.Column;
import schema.Schema;

public class Drop {
	//ie: if exists
	public static void run(String tableName, boolean ie) {
		if (removeSchema(tableName, ie))
			removeFiles(tableName);
	}
	
	/**
	 * @return if success
	 */
	private static boolean removeSchema(String tableName, boolean ie) {
		if (!Schema.HaveSchema(tableName)) {
			if (!ie)
				throw new RuntimeException("you tried to drop a non existing table without the IF EXISTS");
			return false;
		}
		Schema.RemoveSchema(tableName);
		return true;
	}
	
	private static void removeFiles(String tableName) {
		File file = new File(Main.rootdir + "\\" + tableName);
		for(String s: file.list()){
		    File currentFile = new File(file.getPath(),s);
		    currentFile.delete();
		}
		file.delete();
	}
}