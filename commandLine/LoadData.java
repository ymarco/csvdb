package commandLine;

import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import schema.Column2;
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
			Column2[] columns = new Column2[schema.size()];
			for (int i = 0; i < columns.length; i++) {
				JSONObject col = (JSONObject) schema.get(i);
				String field = (String) col.get("field");
				columns[i] = new Column2(VarType.toVarType((String) col.get("type")), field, Main.rootdir + "\\" + table.getName() + "\\" + field);
			}
			Schema.AddSchema(table.getName(), columns);
		}
	}
}
