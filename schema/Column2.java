package schema;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import commandLine.Main;
import exceptions.CsvdbException;

public class Column2 {
	public final VarType type;
	public final String name;

	public Object min;
	public Object max;
	public float avg;
	public Object sum;
	public long count;
	public String filePath;

	public Column2(VarType type, String name, String filePath) {
		this.type = type;
		this.name = name;
		this.filePath = filePath;
		
		if (type == VarType.FLOAT) {
			min = Float.MAX_VALUE;
			max = Float.MIN_VALUE;
			sum = 0F;
		}
		if (type == VarType.INT) {
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
			sum = 0L;
		}
		if (type == VarType.TIMESTAMP) {
			min = -1L;
			max = 0L;
			sum = 0L;
		}
		
		
	}
	
	//TODO add aggrate
	
	
	
	long[] valuesI;
	double[] valuesF;
	long[] valuesTS;
	String[] valuesV;
	
	
	public void loadToMemoryI() {
		try {
			List<Long> values = new ArrayList<>();
			DataInputStream input = new DataInputStream(new FileInputStream(filePath));
			while (input.available() > 0)
				values.add(input.readLong());
			input.close();
			valuesI = new long[values.size()];
			for (int i = 0; i < values.size(); i++)
				valuesI[i] = values.get(i);
		} catch (IOException e) {
			throw new CsvdbException("column file erorr");
		}
	}
	
	public void loadToMemoryF() {
		try {
			List<Double> values = new ArrayList<>();
			DataInputStream input = new DataInputStream(new FileInputStream(filePath));
			while (input.available() > 0)
				values.add(input.readDouble());
			input.close();
			valuesF = new double[values.size()];
			for (int i = 0; i < values.size(); i++)
				valuesF[i] = values.get(i);
		} catch (IOException e) {
			throw new CsvdbException("column file erorr");
		}
	}
	
	public void loadToMemoryTS() {
		try {
			List<Long> values = new ArrayList<>();
			DataInputStream input = new DataInputStream(new FileInputStream(filePath));
			while (input.available() > 0)
				values.add(input.readLong());
			input.close();
			valuesTS = new long[values.size()];
			for (int i = 0; i < values.size(); i++)
				valuesTS[i] = values.get(i);
		} catch (IOException e) {
			throw new CsvdbException("column file erorr");
		}
	}
	
	
	//TODO order: return new indexes
//	public int[] order(boolean lowToHight) {
//		
//	}
}
