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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * GroupUsersBody
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")
public class GroupUsersBody {
  @JsonProperty("group_id")
  private Integer groupId = null;

  @JsonProperty("user_id")
  private Integer userId = null;

  @JsonProperty("admin")
  private Boolean admin = null;

  public GroupUsersBody groupId(Integer groupId) {
    this.groupId = groupId;
    return this;
  }

   /**
   * Group ID to add user to.
   * @return groupId
  **/
  @Schema(required = true, description = "Group ID to add user to.")
  public Integer getGroupId() {
    return groupId;
  }

  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

  public GroupUsersBody userId(Integer userId) {
    this.userId = userId;
    return this;
  }

   /**
   * User ID to add to group.
   * @return userId
  **/
  @Schema(required = true, description = "User ID to add to group.")
  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public GroupUsersBody admin(Boolean admin) {
    this.admin = admin;
    return this;
  }

   /**
   * Is the user a group administrator?
   * @return admin
  **/
  @Schema(description = "Is the user a group administrator?")
  public Boolean isAdmin() {
    return admin;
  }

  public void setAdmin(Boolean admin) {
    this.admin = admin;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupUsersBody groupUsersBody = (GroupUsersBody) o;
    return Objects.equals(this.groupId, groupUsersBody.groupId) &&
        Objects.equals(this.userId, groupUsersBody.userId) &&
        Objects.equals(this.admin, groupUsersBody.admin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, userId, admin);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GroupUsersBody {\n");
    
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    admin: ").append(toIndentedString(admin)).append("\n");
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
