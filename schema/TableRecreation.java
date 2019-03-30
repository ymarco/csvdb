package schema;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cmd.Main;
import utils.FilesUtils;

//TODO
public class TableRecreation {
	private List<Long> rows = new ArrayList<Long>();
	private List<Long> orderColumnLong = new ArrayList<Long>();
	private List<Double> orderColumnDouble = new ArrayList<Double>();
	private List<String> orderColumnString = new ArrayList<String>();
	private VarType orderType;

	private static final int MAX_BYTES_TO_SAVE = 1000;
	private int byte_saving = 0;
	private int fileCount = 0;
	private boolean smallToBig;

	public TableRecreation(boolean smallToBig) {
		this.smallToBig = smallToBig;
		new File(Main.rootdir + "\\#sort").mkdir();
	}

	public void addRow(long row, DBVar var){
		rows.add(row);
		switch (orderType) {
		case INT:
			orderColumnLong.add(var.i);
			byte_saving += 8;
			break;
		case TIMESTAMP:
			orderColumnLong.add(var.ts);
			byte_saving += 8;
			break;
		case FLOAT:
			orderColumnDouble.add(var.f);
			byte_saving += 8;
			break;
		case VARCHAR:
			orderColumnString.add(var.s);
			byte_saving += var.s.length() / 2;
			break;
		}
		if (byte_saving > MAX_BYTES_TO_SAVE)
			pushData();
	}

	public void saveData() {
		pushData();
		merge();
	}

	private void pushData() {
		sort();
		writeToFile();
		rows.clear();
		orderColumnLong.clear();
		orderColumnDouble.clear();
		orderColumnString.clear();
	}

	private void sort() {
		switch (orderType) {
		case FLOAT:
			quickSortDouble(0, rows.size());
			break;
		case VARCHAR:
			quickSortString(0, rows.size());
			break;
		default:
			quickSortLong(0, rows.size());
			break;
		}
	}

	// sort from start to end - 1
	private void quickSortLong(int start, int end) {
		if (start +  1 <= end)
			return;
		long pivot = orderColumnLong.get(start);
		long[] sortedColumns = new long[end - start];
		long[] sortedRows = new long[end - start];
		int litInd = start;
		int bigInd = end - 1;
		for (int i = start + 1; i < end; i++) {
			long item = orderColumnLong.get(i);
			if ((item < pivot) == smallToBig ) { // boolean == boolean is like boolean <-> boolean 
				sortedColumns[litInd] = item;
				sortedRows[litInd] = rows.get(i);
				litInd++;
			} else {
				sortedColumns[bigInd] = item;
				sortedRows[bigInd] = rows.get(i);
				bigInd--;
			}
		}
		//now litInd == bigInd
		sortedColumns[litInd] = pivot;
		sortedRows[litInd] = rows.get(litInd);

		for (int i = start + 1; i < end; i++) {
			orderColumnLong.set(i, sortedColumns[i]);
			rows.set(i, sortedRows[i]);
		}
		quickSortLong(start, litInd);
		quickSortLong(litInd + 1, end);
	}

	private void quickSortDouble(int start, int end) {
		if (start +  1 <= end)
			return;
		double pivot = orderColumnDouble.get(start);
		double[] sorted = new double[end - start];
		int litInd = start;
		int bigInd = end - 1;
		for (int i = start + 1; i < end; i++) {
			double item = orderColumnDouble.get(i);
			if ((item < pivot) == smallToBig)
				sorted[litInd++] = item;
			else
				sorted[bigInd--] = item;
		}
		//now litInd == bigInd
		sorted[litInd] = pivot;

		for (int i = start + 1; i < end; i++)
			orderColumnDouble.set(i, sorted[i]);
		quickSortLong(start, litInd);
		quickSortLong(litInd + 1, end);
	}

	private void quickSortString(int start, int end) {
		if (start +  1 <= end)
			return;
		String pivot = orderColumnString.get(start);
		String[] sorted = new String[end - start];
		int litInd = start;
		int bigInd = end - 1;
		for (int i = start + 1; i < end; i++) {
			String item = orderColumnString.get(i);
			if ((pivot.compareTo(item) < 0) == smallToBig)
				sorted[litInd++] = item;
			else
				sorted[bigInd--] = item;
		}
		//now litInd == bigInd
		sorted[litInd] = pivot;

		for (int i = start + 1; i < end; i++)
			orderColumnString.set(i, sorted[i]);
		quickSortLong(start, litInd);
		quickSortLong(litInd + 1, end);
	}


	//	ArrayList<Long> quickSort(ArrayList<Long> list)
	//	{
	//	    if (list.size() <= 1) 
	//	        return list; // Already sorted  
	//
	//	    ArrayList<Long> sorted = new ArrayList<Long>();
	//	    ArrayList<Long> lesser = new ArrayList<Long>();
	//	    ArrayList<Long> greater = new ArrayList<Long>();
	//	    long pivot = list.get(list.size()-1);
	//	    for (int i = 0; i < list.size()-1; i++)
	//	    {
	//	        if (list.get(i) < pivot)
	//	            lesser.add(list.get(i));    
	//	        else
	//	            greater.add(list.get(i));   
	//	    }
	//
	//	    lesser = quickSort(lesser);
	//	    greater = quickSort(greater);
	//
	//	    lesser.add(pivot);
	//	    lesser.addAll(greater);
	//	    sorted = lesser;
	//
	//	    return sorted;
	//	}


	private void writeToFile() {
		if (rows.size() == 0)
			return;
		try {

			DataOutputStream rowsFile = new DataOutputStream(new FileOutputStream(Main.rootdir + "\\#sort\\rows" + fileCount + ".ri"));
			for (long row : rows)
				rowsFile.writeLong(row);
			rowsFile.close();

			if (orderType == VarType.VARCHAR) {
				BufferedWriter colFile = new BufferedWriter(new FileWriter(Main.rootdir + "\\#sort\\col" + fileCount + Main.columnFilesExtensios));
				for (String item : orderColumnString)
					colFile.write(FilesUtils.endoceStringForWriting(item));
				colFile.close();
			} else  {
				DataOutputStream colFile = new DataOutputStream(new FileOutputStream(Main.rootdir + "\\#sort\\col" + fileCount + Main.columnFilesExtensios));
				if (orderType == VarType.FLOAT)
					for (double item : orderColumnDouble)
						rowsFile.writeDouble(item);
				else
					for (long item : orderColumnLong)
						rowsFile.writeLong(item);
				colFile.close();
			}
		} catch (IOException e) {

		}




		fileCount++;
	}

	private void merge() {
		try {
			switch (orderType) {
			case FLOAT:

				break;
			case VARCHAR:

				break;
			default:
				mergeLong();
				break;
			}



		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mergeLong() throws IOException {
		//		DataOutputStream mergedColsFile = new DataOutputStream(new FileOutputStream(Main.rootdir + "\\#sort\\allRows.ri"));
		DataOutputStream mergedRowsFile = new DataOutputStream(new FileOutputStream(Main.rootdir + "\\#sort\\allRows.ri"));
		DataInputStream[] colsFile = new DataInputStream[fileCount];
		DataInputStream[] rowsFile = new DataInputStream[fileCount];
		for (int i = 0; i < colsFile.length; i++)
			colsFile[i] = new DataInputStream(new FileInputStream(Main.rootdir + "\\#sort\\col" + i + Main.columnFilesExtensios));
		for (int i = 0; i < colsFile.length; i++)
			rowsFile[i] = new DataInputStream(new FileInputStream(Main.rootdir + "\\#sort\\rows" + i + ".ri"));


		int donedFileCount = 0;
		long[] currentItems = new long[fileCount];
		for (int i = 0; i < currentItems.length; i++)
			currentItems[i] = colsFile[i].readLong();
		while (donedFileCount < fileCount) {
			int minItemInd = -1;
			for (int i = 0; i < currentItems.length; i++)
				if (colsFile[i] != null && (minItemInd == -1 || currentItems[i] > currentItems[minItemInd]))
					minItemInd = i;
			if (colsFile[minItemInd].available() > 0) {
				mergedRowsFile.writeLong(rowsFile[minItemInd].readLong());
			} else {
				colsFile[minItemInd].close();
				rowsFile[minItemInd].close();
				colsFile[minItemInd] = null;
				rowsFile[minItemInd] = null;
				donedFileCount++;
			}
		}
		
		mergedRowsFile.close();
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
