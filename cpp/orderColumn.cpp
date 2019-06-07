//#include "orderColumn.h"
#include <string>
#include <iostream>
#include <fstream>

class OrderColumn {
	public void orderAndWrite(long indexes[], long column[], std::String toFile) {
		std::fstream out(toFile, std::ios::binary);
		for each (long index in indexes)
			myFile.write(column[index], sizeof(long));
		out.close();
	}
};

