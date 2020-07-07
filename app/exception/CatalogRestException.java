package exception;

public class CatalogRestException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
	
	public CatalogRestException() {
		super(INTERNAL_SERVER_ERROR);
	}

	public CatalogRestException(String msg) {
		super(msg);
	}
	
	public CatalogRestException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
