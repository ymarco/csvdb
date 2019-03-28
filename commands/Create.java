package commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cmd.Main;
import schema.Column;
import schema.Schema;

public class Create {
	//ine: if not exists
	public static void execute(String tableName, boolean ine, Column[] columns) {
		createSchema(tableName, ine, columns);
		createJson(tableName, columns);
	}
	
	private static void createSchema(String tableName, boolean ine, Column[] columns) {
		if (Schema.HaveSchema(tableName) && !ine)
			throw new RuntimeException("you tried to create an existing table without the IF NOT EXIST");
		Schema.AddSchema(tableName, columns);
	}
	
	private static void createJson(String tableName, Column[] columns) {
		JSONArray schema = new JSONArray();
		for (int i = 0; i < columns.length; i++) {
			JSONObject col = new JSONObject();
			col.put("field", columns[i].getName());
			col.put("type", columns[i].getType().toString());
			schema.add(col);
		}
		
		JSONObject all = new JSONObject();
		all.put("schema", schema);
		
		String path = Main.rootdir + "/" + tableName + "/table.json";
		new File(path).mkdir();
		try (FileWriter file = new FileWriter("f:\\test.json")) {

            file.write(all.toJSONString());
            file.flush();

        } catch (IOException e) {}
	}
}