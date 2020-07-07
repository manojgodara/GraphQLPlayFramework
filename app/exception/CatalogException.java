package exception;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.language.SourceLocation;
import play.Logger;
import play.Logger.ALogger;

public class CatalogException extends GraphQLException implements GraphQLError {

	private static final long serialVersionUID = 1L;
	
	private static final ALogger logger = Logger.of(CatalogException.class);
	
	private String code = "INTERNAL_SERVER_EXCEPTION";

	public CatalogException(String code, String message) {
		super(message);
		this.code = code;
	}
	
	public CatalogException(String message, Throwable exception) {
		super(message, exception);
	}

	@Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> customAttributes = new LinkedHashMap<>();
        customAttributes.put("code", this.code);
        return customAttributes;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }
}
