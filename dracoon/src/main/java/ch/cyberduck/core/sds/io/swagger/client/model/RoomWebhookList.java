/*
 * DRACOON
 * REST Web Services for DRACOON<br>Version: 4.20.1  - built at: 2020-04-05 23:00:17<br><br><a title='Developer Information' href='https://developer.dracoon.com'>Developer Information</a>&emsp;&emsp;<a title='Get SDKs on GitHub' href='https://github.com/dracoon'>Get SDKs on GitHub</a>
 *
 * OpenAPI spec version: 4.20.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.sds.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import ch.cyberduck.core.sds.io.swagger.client.model.Range;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomWebhook;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * List of webhooks
 */
@ApiModel(description = "List of webhooks")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class RoomWebhookList {
  @JsonProperty("range")
  private Range range = null;

  @JsonProperty("items")
  private List<RoomWebhook> items = new ArrayList<>();

  public RoomWebhookList range(Range range) {
    this.range = range;
    return this;
  }

   /**
   * Range
   * @return range
  **/
  @ApiModelProperty(required = true, value = "Range")
  public Range getRange() {
    return range;
  }

  public void setRange(Range range) {
    this.range = range;
  }

  public RoomWebhookList items(List<RoomWebhook> items) {
    this.items = items;
    return this;
  }

  public RoomWebhookList addItemsItem(RoomWebhook itemsItem) {
    this.items.add(itemsItem);
    return this;
  }

   /**
   * List of webhooks
   * @return items
  **/
  @ApiModelProperty(required = true, value = "List of webhooks")
  public List<RoomWebhook> getItems() {
    return items;
  }

  public void setItems(List<RoomWebhook> items) {
    this.items = items;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoomWebhookList roomWebhookList = (RoomWebhookList) o;
    return Objects.equals(this.range, roomWebhookList.range) &&
        Objects.equals(this.items, roomWebhookList.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(range, items);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoomWebhookList {\n");
    
    sb.append("    range: ").append(toIndentedString(range)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
