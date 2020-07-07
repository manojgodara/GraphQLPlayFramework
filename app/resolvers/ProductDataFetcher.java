package resolvers;

import com.google.common.base.Strings;
import entity.Product;
import entity.ProductDetail;
import entity.ProductInstance;
import exception.CatalogException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import play.Logger;
import play.Logger.ALogger;

public class ProductDataFetcher {

	private final ALogger logger = Logger.of(ProductDataFetcher.class);


	/**
	 * <b>Description</b> : Get offering by offeringId
	 *
	 * @return Product CompletableFuture
	 */
	public DataFetcher<Product> productById() {
		return env -> {
			CompletableFuture<Product> promise = new CompletableFuture<>();

			String productId = env.getArgument("id");
			if (Strings.isNullOrEmpty(productId)) {
				throw new CatalogException("BAD_USER_INPUT", "ID: Required field.");
			}
			Long id = Long.valueOf(productId);

			DataFetchingFieldSelectionSet selectionSet = env.getSelectionSet();

			Product product = new Product();
			product.setId(id);
			product.setName("ProductName");
			product.setCategoryName("The Category Name");
			product.setDescription("Here is the product description.");

			if (selectionSet.contains("details")) {
				product.setDetails(getProductDetails(id));
			}
			return product;
		};
	}

	/**
	 * <b>Description</b> : Get offering details by offeringId
	 *
	 * @return ProductDetail CompletableFuture
	 */
	public DataFetcher<Collection<ProductDetail>> productDetail() {

		return env -> {
			Product product = env.getSource();
			Long id = product.getId();
			return getProductDetails(id);
		};
	}

	/**
	 * <b>Description</b> : Get offering instance by requestId
	 *
	 * @return ProductInstance CompletableFuture
	 */
	public DataFetcher<CompletionStage<ProductInstance>> productInstance() {
		return env -> {
			CompletableFuture<ProductInstance> promise = new CompletableFuture<>();
			Long requestId = Long.valueOf(env.getArgument("id"));
			ProductInstance instance = new ProductInstance();
			instance.id = requestId;
			instance.name = "instanceName";
			promise.complete(instance);
			return promise;
		};
	}

	public DataFetcher<CompletableFuture<Collection<Product>>> relatedOfferings() {
		return env -> {
			Product product = env.getSource();
			Long offeringId = product.getId();
			CompletableFuture<Collection<Product>> promise = new CompletableFuture<>();

			List<Product> offerings = new ArrayList<>();
			Product offering = new Product();
			offering.setId(offeringId);
			offering.setCategoryName("The Category Name");
			offering.setDescription("Here is the product description.");
			offerings.add(offering);
			promise.complete(offerings);
			return promise;
		};
	}

	private List<ProductDetail> getProductDetails(Long id) {
		List<ProductDetail> details = new ArrayList<>();
		ProductDetail detail = new ProductDetail();
		detail.description = "The product detailed description.";
		detail.card = true;
		detail.id = id;
		detail.display = "display";
		detail.image = "NA";
		detail.name = "product details";
		details.add(detail);
		return details;
	}
}
