/*
 * Storegate.Web
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v4
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.storegate.io.swagger.client.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A CreateMediaFolderRequest request object
 */
@ApiModel(description = "A CreateMediaFolderRequest request object")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:06:08.665+02:00")
public class CreateMediaFolderRequest {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("description")
  private String description = null;

  public CreateMediaFolderRequest name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The Name
   * @return name
  **/
  @ApiModelProperty(required = true, value = "The Name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CreateMediaFolderRequest description(String description) {
    this.description = description;
    return this;
  }

   /**
   * The Description
   * @return description
  **/
  @ApiModelProperty(value = "The Description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateMediaFolderRequest createMediaFolderRequest = (CreateMediaFolderRequest) o;
    return Objects.equals(this.name, createMediaFolderRequest.name) &&
        Objects.equals(this.description, createMediaFolderRequest.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateMediaFolderRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

