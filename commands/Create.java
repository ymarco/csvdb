package commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import commandLine.Main;
import schema.Column2;
import schema.Schema;

public class Create implements Command {
	private String tableName;
	private boolean ine; //if not exists
	private Column2[] columns;
	
	public Create(String tableName, boolean ine, Column2[] columns) {
		this.tableName = tableName;
		this.ine = ine;
		this.columns = columns;
	}
	
	public void run() {
		if (createSchema(tableName, ine, columns))
			createJson(tableName, columns);
	}
	
	/**
	 * @return if success
	 */
	private boolean createSchema(String tableName, boolean ine, Column2[] columns) {
		if (Schema.HaveSchema(tableName)) {
			if (!ine)
				throw new RuntimeException("you tried to create an existing table without the IF NOT EXISTS");
			return false;
		}
		Schema.AddSchema(tableName, columns);
		return true;
	}
	
	private void createJson(String tableName, Column2[] columns) {
		JSONArray schema = new JSONArray();
		for (int i = 0; i < columns.length; i++) {
			JSONObject col = new JSONObject();
			col.put("field", columns[i].name);
			col.put("type", columns[i].type.toString());
			schema.add(col);
		}
		
		JSONObject all = new JSONObject();
		all.put("schema", schema);
		
		new File(Main.rootdir + "\\" + tableName).mkdir();
		String path = Main.rootdir + "\\" + tableName + "\\table.json";
		try (FileWriter file = new FileWriter(path)) {

            file.write(all.toJSONString());
            file.flush();

        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
}