package entity;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import common.HashCodeUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

	private Long id;
	private String categoryName;
	private String description;
	private String image;
	private String name;
	private Long parentId;
	private Collection<entity.ProductDetail> details;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCategoryName() { 
		return categoryName;
	}

	@JsonSetter("parent_offering_name")
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		return description;
	}

	@JsonSetter("offering_description")
	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	@JsonSetter("image_file")
	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	@JsonSetter("offering_name")
	public void setName(String name) {
		this.name = name;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Collection<entity.ProductDetail> getDetails() {
		return details;
	}

	public void setDetails(Collection<entity.ProductDetail> details) {
		this.details = details;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof Product)) {
			return false;
		}

		Product other = (Product) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return HashCodeUtils.hashCode(id);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
