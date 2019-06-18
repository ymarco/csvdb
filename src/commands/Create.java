package commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import schema.Column;
import schema.Schema;

public class Create implements Command {
	private String tableName;
	private boolean ine; //if not exists
	private Column[] columns;

	public Create(String tableName, boolean ine, Column[] columns) {
		this.tableName = tableName;
		this.ine = ine;
		this.columns = columns;
	}

	public void run() {
		Schema schema = createSchema(tableName, ine, columns);
		createJson(schema, columns);
	}

	/**
	 * @return if success
	 */
	private Schema createSchema(String tableName, boolean ine, Column[] columns) {
		if (Schema.HaveSchema(tableName)) {
			if (!ine)
				throw new RuntimeException("you tried to create an existing table without the IF NOT EXISTS");
		}

		Schema schema = new Schema(tableName, columns);
		Schema.AddSchema(schema);
		return schema;
	}

	private void createJson(Schema schema, Column[] columns) {
		JSONArray jsonSchema = new JSONArray();
		for (Column column : columns) {
			JSONObject col = new JSONObject();
			col.put("field", column.name);
			col.put("type", column.type.toString());
			jsonSchema.add(col);
		}

		JSONObject all = new JSONObject();
		all.put("schema", jsonSchema);

		new File(schema.getTablePath()).mkdir();
		String path = String.join(File.separator, schema.getTablePath(), "table.json");
		try (FileWriter file = new FileWriter(path)) {

			file.write(all.toJSONString());
			file.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}