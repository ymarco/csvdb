package schema;

import java.util.ArrayList;

//TODO
public class TableRecreation {
	private Schema source;
	private Column[] columns;
	private ArrayList<long> rows;

	public void addRow(long row){
		rows.add(row);
	}

	public Schema createSchema(){
		return null;

	}
	public void createTable() {
		/* some pseudo code for you:
		file[] src_files;
		for i in range(columns.length):
			src_files[i] = column[i].file.open()
		file[] dst_files;
		for file in dst_files:
			dst_files[i] = open("dst_location/"+column.name)

		for (long r : rows) {
			for i in range(columns.len):
				dst_files[i].writeRow(src_files[i].atRow(r))
		 */
	}
}
