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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A ExtendedUserContents object
 */
@ApiModel(description = "A ExtendedUserContents object")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:06:08.665+02:00")
public class ExtendedUserContents {
  @JsonProperty("users")
  private List<ExtendedUser> users = null;

  @JsonProperty("totalRowCount")
  private Integer totalRowCount = null;

  @JsonProperty("numberOfUsers")
  private Integer numberOfUsers = null;

  public ExtendedUserContents users(List<ExtendedUser> users) {
    this.users = users;
    return this;
  }

  public ExtendedUserContents addUsersItem(ExtendedUser usersItem) {
    if (this.users == null) {
        this.users = new ArrayList<>();
    }
    this.users.add(usersItem);
    return this;
  }

   /**
   * The list of users.
   * @return users
  **/
  @ApiModelProperty(value = "The list of users.")
  public List<ExtendedUser> getUsers() {
    return users;
  }

  public void setUsers(List<ExtendedUser> users) {
    this.users = users;
  }

  public ExtendedUserContents totalRowCount(Integer totalRowCount) {
    this.totalRowCount = totalRowCount;
    return this;
  }

   /**
   * Total number of users.
   * @return totalRowCount
  **/
  @ApiModelProperty(value = "Total number of users.")
  public Integer getTotalRowCount() {
    return totalRowCount;
  }

  public void setTotalRowCount(Integer totalRowCount) {
    this.totalRowCount = totalRowCount;
  }

  public ExtendedUserContents numberOfUsers(Integer numberOfUsers) {
    this.numberOfUsers = numberOfUsers;
    return this;
  }

   /**
   * Number of total users available
   * @return numberOfUsers
  **/
  @ApiModelProperty(value = "Number of total users available")
  public Integer getNumberOfUsers() {
    return numberOfUsers;
  }

  public void setNumberOfUsers(Integer numberOfUsers) {
    this.numberOfUsers = numberOfUsers;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExtendedUserContents extendedUserContents = (ExtendedUserContents) o;
    return Objects.equals(this.users, extendedUserContents.users) &&
        Objects.equals(this.totalRowCount, extendedUserContents.totalRowCount) &&
        Objects.equals(this.numberOfUsers, extendedUserContents.numberOfUsers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(users, totalRowCount, numberOfUsers);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExtendedUserContents {\n");
    
    sb.append("    users: ").append(toIndentedString(users)).append("\n");
    sb.append("    totalRowCount: ").append(toIndentedString(totalRowCount)).append("\n");
    sb.append("    numberOfUsers: ").append(toIndentedString(numberOfUsers)).append("\n");
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

