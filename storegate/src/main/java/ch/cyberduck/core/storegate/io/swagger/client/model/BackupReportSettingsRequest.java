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
 * 
 */
@ApiModel(description = "")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:06:08.665+02:00")
public class BackupReportSettingsRequest {
  @JsonProperty("interval")
  private String interval = null;

  @JsonProperty("emails")
  private List<String> emails = null;

  public BackupReportSettingsRequest interval(String interval) {
    this.interval = interval;
    return this;
  }

   /**
   * 
   * @return interval
  **/
  @ApiModelProperty(value = "")
  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public BackupReportSettingsRequest emails(List<String> emails) {
    this.emails = emails;
    return this;
  }

  public BackupReportSettingsRequest addEmailsItem(String emailsItem) {
    if (this.emails == null) {
        this.emails = new ArrayList<>();
    }
    this.emails.add(emailsItem);
    return this;
  }

   /**
   * 
   * @return emails
  **/
  @ApiModelProperty(value = "")
  public List<String> getEmails() {
    return emails;
  }

  public void setEmails(List<String> emails) {
    this.emails = emails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BackupReportSettingsRequest backupReportSettingsRequest = (BackupReportSettingsRequest) o;
    return Objects.equals(this.interval, backupReportSettingsRequest.interval) &&
        Objects.equals(this.emails, backupReportSettingsRequest.emails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interval, emails);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackupReportSettingsRequest {\n");
    
    sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
    sb.append("    emails: ").append(toIndentedString(emails)).append("\n");
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

