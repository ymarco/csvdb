package commandLine;

import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import schema.Column2;
import schema.DBVar;
import schema.Schema;

public class LoadData {
	
	public static void load() {
		File rootFolder = new File(Main.rootdir);
		for (File tableDir : rootFolder.listFiles()) {
			if (tableDir.getName().startsWith("#"))
				continue;
			File dataFile = new File(String.join(File.separator,tableDir.getPath() + "table.json"));
			if (!dataFile.exists())
				continue;
			JSONParser jsonParser = new JSONParser();
			Object data;
			try {
				data = jsonParser.parse(new FileReader(dataFile));
			} catch (Exception e) {
				System.out.println("Load Exception");
				e.printStackTrace();
				continue;
			}
			JSONArray JSONSchema = (JSONArray) ((JSONObject) data).get("schema");
			Column2[] columns = new Column2[JSONSchema.size()];
			for (int i = 0; i < columns.length; i++) {
				JSONObject col = (JSONObject) JSONSchema.get(i);
				String field = (String) col.get("field");
				columns[i] = new Column2(DBVar.Type.toVarType((String) col.get("type")), field, "");
			}
			Schema.AddSchema(new Schema(tableDir.getName(), columns));
		}
	}
}
