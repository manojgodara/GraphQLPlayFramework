package common;

public class HashCodeUtils {

  private HashCodeUtils() {
  }

  public static int hashCode(Object... objects) {
    long hc = 1;
    for (Object o : objects) {
      if (o != null) {
        hc = (31L * hc) + o.hashCode();
      }
    }

    return (int) (hc ^ (hc >> 32));
  }

}
