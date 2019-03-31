package cmd;

import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import schema.Column;
import schema.Schema;
import schema.VarType;

public class LoadData {
	
	public static void load() {
		File rootFolder = new File(Main.rootdir);
		for (File table : rootFolder.listFiles()) {
			if (table.getName().startsWith("#"))
				continue;
			File dataFile = new File(table.getPath() + "\\table.json");
			if (!dataFile.exists())
				continue;
			JSONParser jsonParser = new JSONParser();
			Object data = null;
			try {
				data = jsonParser.parse(new FileReader(dataFile));
			} catch (Exception e) {
				System.out.println("Load Exception");
				e.printStackTrace();
				continue;
			}
			JSONArray schema = (JSONArray) ((JSONObject) data).get("schema");
			Column[] columns = new Column[schema.size()];
			for (int i = 0; i < columns.length; i++) {
				JSONObject col = (JSONObject) schema.get(i);
				columns[i] = new Column(VarType.toVarType((String) col.get("type")), (String) col.get("field"));
			}
			Schema.AddSchema(table.getName(), columns);
		}
	}
}
