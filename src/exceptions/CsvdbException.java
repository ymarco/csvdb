package exceptions;

public class CsvdbException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CsvdbException() {
		super();
	}

	public CsvdbException(String messge) {
		super(messge);
	}
}
