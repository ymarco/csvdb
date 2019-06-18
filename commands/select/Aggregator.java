package commands.select;

import schema.DBVar;
import schema.dbvars.DBFloat;
import schema.dbvars.DBInt;
import schema.dbvars.DBTS;

import java.util.Comparator;

public interface Aggregator {
	void aggregate(DBVar var);

	DBVar getVal();

	void reset();

	default boolean isEmpty() {
		return false;
	}

	class Min implements Aggregator {
		DBVar val = null;


		@Override
		public void aggregate(DBVar var) {
			if (val == null) val = var;
			val = (var.comparator().compare(val, var) < 0 ? val : var);
		}

		@Override
		public DBVar getVal() {
			return val;
		}

		@Override
		public void reset() {
			val = null;
		}
	}

	class Max implements Aggregator {
		DBVar val = null;


		@Override
		public void aggregate(DBVar var) {
			if (val == null) val = var;
			val = (var.comparator().compare(val, var) > 0 ? val : var);
		}

		@Override
		public DBVar getVal() {
			return val;
		}

		@Override
		public void reset() {
			val = null;
		}
	}

	class Sum implements Aggregator {
		double sum = 0;

		@Override
		public void aggregate(DBVar var) {
			DBVar.Type type = var.getType();
			switch (type) {
				case INT:
					sum += ((DBInt) var).val;
					break;
				case TS:
					sum += ((DBTS) var).val;
					break;
				case FLOAT:
					sum += ((DBFloat) var).val;
					break;
			}
		}

		@Override
		public DBVar getVal() {
			return new DBFloat(sum);
		}

		@Override
		public void reset() {
			sum = 0;
		}
	}

	class Avg implements Aggregator {
		double sum = 0;
		long elementsNumber = 0;

		@Override
		public void aggregate(DBVar var) {
			switch (var.getType()) {
				case INT:
					sum += ((DBInt) var).val;
				case TS:
					sum += ((DBTS) var).val;
				case FLOAT:
					sum += ((DBFloat) var).val;
			}
			elementsNumber++;
		}

		@Override
		public DBVar getVal() {
			return new DBFloat(sum / elementsNumber);
		}

		@Override
		public void reset() {
			sum = 0;
			elementsNumber = 0;
		}

	}

	class EmptyAgg implements Aggregator {

		DBVar val;

		@Override
		public void aggregate(DBVar var) {
			val = var;
		}

		@Override
		public DBVar getVal() {
			return val;
		}

		@Override
		public void reset() {
			val = null; // doesnt really matter
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	}

	class Count implements Aggregator {

		long count = 0;

		@Override
		public void aggregate(DBVar var) {
			if (!var.isNull())
				count++;
		}

		@Override
		public DBVar getVal() {
			return new DBInt(count);
		}

		@Override
		public void reset() {
			count = 0;
		}
	}
}
