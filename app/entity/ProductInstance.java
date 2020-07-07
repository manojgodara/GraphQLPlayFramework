package entity;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import common.HashCodeUtils;

public class ProductInstance {

	public Long id;
	public Long requestId;
	public Long productId;
	public String note;
	public String name;
	public String subscribedDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long offeringId) {
		this.productId = offeringId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getName() {
		return name;
	}

	public void setName(String offeringName) {
		this.name = offeringName;
	}

	public String getSubscribedDate() {
		return subscribedDate;
	}

	public void setSubscribedDate(String subscribedDate) {
		this.subscribedDate = subscribedDate;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof ProductInstance)) {
			return false;
		}

		ProductInstance other = (ProductInstance) obj;
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
