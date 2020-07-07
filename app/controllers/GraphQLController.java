package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.InvalidSyntaxError;
import graphql.language.SourceLocation;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import graphql.validation.ValidationError;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import play.Logger;
import play.Logger.ALogger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import resolvers.ProductDataFetcher;

/**
 * The servlet/controller acting as the GraphQL endpoint
 */
public class GraphQLController extends Controller {
	
	private static final ALogger logger = Logger.of(GraphQLController.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String SERVER_ERROR = "INTERNAL_SERVER_EXCEPTION";
	private static final String CONTENT_TYPE_APPLICATION_OR_JSON_CHARSET = "application/json";

	private GraphQLSchema schema;
	
	@Inject
	private ProductDataFetcher productDataFetcher;

	@Inject
	private HttpExecutionContext ec;
	
	public CompletionStage<Result> postHandler() throws IOException {
		
		this.schema = buildSchema();
		JsonNode body = request().body().asJson();
		
		if (body == null) {
			logger.error("Query missing in request body "+request().path());
			return CompletableFuture.completedFuture(badRequest("Query missing in request body "+request().path()));
		}
		GraphQLRequest graphQLRequest = MAPPER.readValue(body.traverse(), GraphQLRequest.class);
		
		Map<String,Object> variables = graphQLRequest.getVariables();
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(graphQLRequest.getQuery())
                .operationName(graphQLRequest.getOperationName())
                .variables(variables)
                .build();

		return query(executionInput);
	}
	
	private GraphQLSchema buildSchema() throws UnsupportedEncodingException {
		ClassLoader classLoader = getClass().getClassLoader();
		String path = URLDecoder.decode(classLoader.getResource("schema.graphqls").getFile(), "UTF-8");
		TypeDefinitionRegistry typeRegistry = new graphql.schema.idl.SchemaParser().parse(new File(path));
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
	}
	
	private RuntimeWiring buildWiring() {
		PropertyDataFetcher.clearReflectionCache();
		return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("productById", productDataFetcher.productById()))
								.type(TypeRuntimeWiring.newTypeWiring("Product")
										.dataFetcher("details", productDataFetcher.productDetail())
										.dataFetcher("instance", productDataFetcher.productInstance())
										.dataFetcher("related", productDataFetcher.relatedOfferings()))
                .build();
	}

	private GraphQL newGraphQL(GraphQLSchema schema) {
		return GraphQL.newGraphQL(schema)
                .build();
	}
	
	private CompletableFuture<Result> query(ExecutionInput executionInput) {
		CompletableFuture<Result> promise = new CompletableFuture<Result>();

		final CompletableFuture<ExecutionResult> executionResult = newGraphQL(schema).executeAsync(executionInput);
		executionResult.whenCompleteAsync((result, exce) -> {
			if (exce != null) {
				logger.error("Error while executing the query " + exce);
				promise.complete(badRequest("INTERNAL_SERVER_ERROR"));
				return;
			}
			final List<GraphQLError> errors = result.getErrors();
			final Object data = result.getData();

			try {
				promise.complete(ok(MAPPER.writeValueAsString(createResultFromDataAndErrors(data, errors)))
						.as(CONTENT_TYPE_APPLICATION_OR_JSON_CHARSET));
			} catch (JsonProcessingException e) {
				logger.error("Error while parsing the execution result " + e);
				promise.complete(badRequest("Error while parsing the Graphql query execution result."));
				return;
			}
		}, ec.current());
		return promise;
		 
	}

    private Map<String, Object> createResultFromDataAndErrors(Object data, List<GraphQLError> errors) {

        final Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        if (errorsPresent(errors)) {
            final List<GraphQLError> clientErrors = filterGraphQLErrors(errors);
            if (clientErrors.size() < errors.size()) {
                // Some errors were filtered out to hide implementation - put a generic error in place.
                errors.stream()
                    .filter(error -> !isClientError(error))
                    .forEach(error -> {
                    	logger.error("Error executing query ({}): {}", error.getClass().getSimpleName(), error.getMessage());
                    	if (error instanceof ExceptionWhileDataFetching) {
                    		ExceptionWhileDataFetching exception = (ExceptionWhileDataFetching) error;
                    		clientErrors.add(new GenericGraphQLError(exception));
                    	} else {
                    		clientErrors.add(error);
                    	}
                    });
            }
            result.put("errors", clientErrors);
        }

        return result;
    }
    
    private boolean errorsPresent(List<GraphQLError> errors) {
        return errors != null && !errors.isEmpty();
    }

    protected List<GraphQLError> filterGraphQLErrors(List<GraphQLError> errors) {
        return errors.stream()
            .filter(this::isClientError)
            .collect(Collectors.toList());
    }

    protected boolean isClientError(GraphQLError error) {
        return error instanceof InvalidSyntaxError || error instanceof ValidationError;
    }

    protected Map<String, Object> transformVariables(GraphQLSchema schema, String query, Map<String, Object> variables) {
        return variables;
    }

    protected static class GraphQLRequest {
        private String query;
        private Map<String, Object> variables = new HashMap<>();
        private String operationName;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }
    }
    
    protected static class GenericGraphQLError implements GraphQLError {
    	
		private static final long serialVersionUID = 1L;
		
		private String message;
    	private ErrorType errorType;
    	private List<Object> path;
    	private List<SourceLocation> location;
    	private Map<String, Object> extentions;
    	private String exception;
    	
    	public GenericGraphQLError(ExceptionWhileDataFetching error) {
    		this.message = error.getMessage();
    		this.errorType = error.getErrorType();
    		this.exception = ExceptionUtils.getStackTrace(error.getException());
    		this.path = error.getPath();
    		this.location = error.getLocations();
    		this.extentions = error.getExtensions();
    		if (this.extentions.isEmpty()) {
    			this.extentions = new LinkedHashMap<>();
    			this.extentions.put("code", SERVER_ERROR);
    		}
    	}
    	
    	public GenericGraphQLError (String message) {
    		this.message = message;
    		this.errorType = ErrorType.DataFetchingException;
    	}

		@Override
		public String getMessage() {
			return this.message;
		}

		@Override
		public List<SourceLocation> getLocations() {
			return this.location;
		}

		@Override
		public ErrorType getErrorType() {
			return this.errorType;
		}
		
		@Override
		public Map<String, Object> getExtensions() {
			return this.extentions;
	    }
		
		@Override
		public List<Object> getPath() {
	        return this.path;
	    }

		public String getException() {
			return exception;
		}

		public void setException(String exception) {
			this.exception = exception;
		}
    }
}
