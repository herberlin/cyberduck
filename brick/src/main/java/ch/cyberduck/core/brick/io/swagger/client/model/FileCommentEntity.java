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
import ch.cyberduck.core.brick.io.swagger.client.model.FileCommentReactionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * List File Comments by path
 */
@Schema(description = "List File Comments by path")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")
public class FileCommentEntity {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("body")
  private String body = null;

  @JsonProperty("reactions")
  private FileCommentReactionEntity reactions = null;

  public FileCommentEntity id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * File Comment ID
   * @return id
  **/
  @Schema(example = "1", description = "File Comment ID")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public FileCommentEntity body(String body) {
    this.body = body;
    return this;
  }

   /**
   * Comment body.
   * @return body
  **/
  @Schema(example = "What a great file!", description = "Comment body.")
  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public FileCommentEntity reactions(FileCommentReactionEntity reactions) {
    this.reactions = reactions;
    return this;
  }

   /**
   * Get reactions
   * @return reactions
  **/
  @Schema(description = "")
  public FileCommentReactionEntity getReactions() {
    return reactions;
  }

  public void setReactions(FileCommentReactionEntity reactions) {
    this.reactions = reactions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileCommentEntity fileCommentEntity = (FileCommentEntity) o;
    return Objects.equals(this.id, fileCommentEntity.id) &&
        Objects.equals(this.body, fileCommentEntity.body) &&
        Objects.equals(this.reactions, fileCommentEntity.reactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, body, reactions);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileCommentEntity {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
    sb.append("    reactions: ").append(toIndentedString(reactions)).append("\n");
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
