/*
 * Files.com API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 0.0.1
 * Contact: support@files.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.cyberduck.core.brick.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import ch.cyberduck.core.brick.io.swagger.client.model.ImageEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * Show Style
 */
@Schema(description = "Show Style")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")
public class StyleEntity {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("logo")
  private ImageEntity logo = null;

  @JsonProperty("thumbnail")
  private ImageEntity thumbnail = null;

  public StyleEntity id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * Style ID
   * @return id
  **/
  @Schema(example = "1", description = "Style ID")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public StyleEntity path(String path) {
    this.path = path;
    return this;
  }

   /**
   * Folder path
   * @return path
  **/
  @Schema(description = "Folder path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public StyleEntity logo(ImageEntity logo) {
    this.logo = logo;
    return this;
  }

   /**
   * Get logo
   * @return logo
  **/
  @Schema(description = "")
  public ImageEntity getLogo() {
    return logo;
  }

  public void setLogo(ImageEntity logo) {
    this.logo = logo;
  }

  public StyleEntity thumbnail(ImageEntity thumbnail) {
    this.thumbnail = thumbnail;
    return this;
  }

   /**
   * Get thumbnail
   * @return thumbnail
  **/
  @Schema(description = "")
  public ImageEntity getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(ImageEntity thumbnail) {
    this.thumbnail = thumbnail;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StyleEntity styleEntity = (StyleEntity) o;
    return Objects.equals(this.id, styleEntity.id) &&
        Objects.equals(this.path, styleEntity.path) &&
        Objects.equals(this.logo, styleEntity.logo) &&
        Objects.equals(this.thumbnail, styleEntity.thumbnail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, path, logo, thumbnail);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StyleEntity {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
    sb.append("    thumbnail: ").append(toIndentedString(thumbnail)).append("\n");
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
