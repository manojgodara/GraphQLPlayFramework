package entity;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import common.HashCodeUtils;

public class ProductDetail {
  public Long id;
  public String description;
  public String display;
  public String image;
  public String link;
  public String linkCaption;
  public String name;
  public String type;
  public Boolean header = false;
  public Boolean card = false;

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null || !(obj instanceof ProductDetail)) {
      return false;
    }

    ProductDetail other = (ProductDetail) obj;
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
