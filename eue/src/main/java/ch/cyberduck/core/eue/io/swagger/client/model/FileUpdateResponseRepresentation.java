/*
 * ReSTFS
 * ReSTFS Open API 3.0 Spec
 *
 * OpenAPI spec version: 1.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.cyberduck.core.eue.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * FileUpdateResponseRepresentation
 */


public class FileUpdateResponseRepresentation {
  @JsonProperty("uploadURI")
  private String uploadURI = null;

  public FileUpdateResponseRepresentation uploadURI(String uploadURI) {
    this.uploadURI = uploadURI;
    return this;
  }

   /**
   * Get uploadURI
   * @return uploadURI
  **/
  @Schema(description = "")
  public String getUploadURI() {
    return uploadURI;
  }

  public void setUploadURI(String uploadURI) {
    this.uploadURI = uploadURI;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileUpdateResponseRepresentation fileUpdateResponseRepresentation = (FileUpdateResponseRepresentation) o;
    return Objects.equals(this.uploadURI, fileUpdateResponseRepresentation.uploadURI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uploadURI);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileUpdateResponseRepresentation {\n");
    
    sb.append("    uploadURI: ").append(toIndentedString(uploadURI)).append("\n");
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
