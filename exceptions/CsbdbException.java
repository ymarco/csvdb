package exceptions;

public class CsbdbException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public CsbdbException() {
		super();
	}
	
	public CsbdbException(String messge) {
		super(messge);
	}
}
