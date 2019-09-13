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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Item containing information about a public available share.
 */
@ApiModel(description = "Item containing information about a public available share.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:06:08.665+02:00")
public class PublicShareInfo {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  /**
   * Indicated the sharetype
   */
  public enum TypeEnum {
    NUMBER_0(0),
    
    NUMBER_1(1),
    
    NUMBER_2(2),
    
    NUMBER_3(3);

    private Integer value;

    TypeEnum(Integer value) {
      this.value = value;
    }

    @JsonValue
    public Integer getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("fileAllowUpload")
  private Boolean fileAllowUpload = null;

  @JsonProperty("contentHidden")
  private Boolean contentHidden = null;

  @JsonProperty("mediaAllowDownload")
  private Boolean mediaAllowDownload = null;

  @JsonProperty("sharedBy")
  private ShareUser sharedBy = null;

  @JsonProperty("partnerId")
  private String partnerId = null;

  @JsonProperty("retailerId")
  private String retailerId = null;

  /**
   * 
   */
  public enum AuthMethodEnum {
    NUMBER_0(0),
    
    NUMBER_1(1),
    
    NUMBER_2(2);

    private Integer value;

    AuthMethodEnum(Integer value) {
      this.value = value;
    }

    @JsonValue
    public Integer getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AuthMethodEnum fromValue(String text) {
      for (AuthMethodEnum b : AuthMethodEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("authMethod")
  private AuthMethodEnum authMethod = null;

  @JsonProperty("fileAllowOfficeOnline")
  private Boolean fileAllowOfficeOnline = null;

  @JsonProperty("fileAllowOfficeOnlineEdit")
  private Boolean fileAllowOfficeOnlineEdit = null;

  public PublicShareInfo id(String id) {
    this.id = id;
    return this;
  }

   /**
   * The share id.
   * @return id
  **/
  @ApiModelProperty(value = "The share id.")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PublicShareInfo name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The share name.
   * @return name
  **/
  @ApiModelProperty(value = "The share name.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PublicShareInfo type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Indicated the sharetype
   * @return type
  **/
  @ApiModelProperty(value = "Indicated the sharetype")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public PublicShareInfo fileAllowUpload(Boolean fileAllowUpload) {
    this.fileAllowUpload = fileAllowUpload;
    return this;
  }

   /**
   * Set to true if the file share allows upload.
   * @return fileAllowUpload
  **/
  @ApiModelProperty(value = "Set to true if the file share allows upload.")
  public Boolean isFileAllowUpload() {
    return fileAllowUpload;
  }

  public void setFileAllowUpload(Boolean fileAllowUpload) {
    this.fileAllowUpload = fileAllowUpload;
  }

  public PublicShareInfo contentHidden(Boolean contentHidden) {
    this.contentHidden = contentHidden;
    return this;
  }

   /**
   * Set to true if the file share allows upload but the content is hidden.
   * @return contentHidden
  **/
  @ApiModelProperty(value = "Set to true if the file share allows upload but the content is hidden.")
  public Boolean isContentHidden() {
    return contentHidden;
  }

  public void setContentHidden(Boolean contentHidden) {
    this.contentHidden = contentHidden;
  }

  public PublicShareInfo mediaAllowDownload(Boolean mediaAllowDownload) {
    this.mediaAllowDownload = mediaAllowDownload;
    return this;
  }

   /**
   * Set to true if the media share allows download of the original image.
   * @return mediaAllowDownload
  **/
  @ApiModelProperty(value = "Set to true if the media share allows download of the original image.")
  public Boolean isMediaAllowDownload() {
    return mediaAllowDownload;
  }

  public void setMediaAllowDownload(Boolean mediaAllowDownload) {
    this.mediaAllowDownload = mediaAllowDownload;
  }

  public PublicShareInfo sharedBy(ShareUser sharedBy) {
    this.sharedBy = sharedBy;
    return this;
  }

   /**
   * Information about the user that created the share
   * @return sharedBy
  **/
  @ApiModelProperty(value = "Information about the user that created the share")
  public ShareUser getSharedBy() {
    return sharedBy;
  }

  public void setSharedBy(ShareUser sharedBy) {
    this.sharedBy = sharedBy;
  }

  public PublicShareInfo partnerId(String partnerId) {
    this.partnerId = partnerId;
    return this;
  }

   /**
   * The PartnerId, used for content/media/configuration
   * @return partnerId
  **/
  @ApiModelProperty(value = "The PartnerId, used for content/media/configuration")
  public String getPartnerId() {
    return partnerId;
  }

  public void setPartnerId(String partnerId) {
    this.partnerId = partnerId;
  }

  public PublicShareInfo retailerId(String retailerId) {
    this.retailerId = retailerId;
    return this;
  }

   /**
   * The Reatiler, used for content/media/configuration
   * @return retailerId
  **/
  @ApiModelProperty(value = "The Reatiler, used for content/media/configuration")
  public String getRetailerId() {
    return retailerId;
  }

  public void setRetailerId(String retailerId) {
    this.retailerId = retailerId;
  }

  public PublicShareInfo authMethod(AuthMethodEnum authMethod) {
    this.authMethod = authMethod;
    return this;
  }

   /**
   * 
   * @return authMethod
  **/
  @ApiModelProperty(value = "")
  public AuthMethodEnum getAuthMethod() {
    return authMethod;
  }

  public void setAuthMethod(AuthMethodEnum authMethod) {
    this.authMethod = authMethod;
  }

  public PublicShareInfo fileAllowOfficeOnline(Boolean fileAllowOfficeOnline) {
    this.fileAllowOfficeOnline = fileAllowOfficeOnline;
    return this;
  }

   /**
   * Is view in Office Online allowed
   * @return fileAllowOfficeOnline
  **/
  @ApiModelProperty(value = "Is view in Office Online allowed")
  public Boolean isFileAllowOfficeOnline() {
    return fileAllowOfficeOnline;
  }

  public void setFileAllowOfficeOnline(Boolean fileAllowOfficeOnline) {
    this.fileAllowOfficeOnline = fileAllowOfficeOnline;
  }

  public PublicShareInfo fileAllowOfficeOnlineEdit(Boolean fileAllowOfficeOnlineEdit) {
    this.fileAllowOfficeOnlineEdit = fileAllowOfficeOnlineEdit;
    return this;
  }

   /**
   * Is edit in Office Online allowed
   * @return fileAllowOfficeOnlineEdit
  **/
  @ApiModelProperty(value = "Is edit in Office Online allowed")
  public Boolean isFileAllowOfficeOnlineEdit() {
    return fileAllowOfficeOnlineEdit;
  }

  public void setFileAllowOfficeOnlineEdit(Boolean fileAllowOfficeOnlineEdit) {
    this.fileAllowOfficeOnlineEdit = fileAllowOfficeOnlineEdit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicShareInfo publicShareInfo = (PublicShareInfo) o;
    return Objects.equals(this.id, publicShareInfo.id) &&
        Objects.equals(this.name, publicShareInfo.name) &&
        Objects.equals(this.type, publicShareInfo.type) &&
        Objects.equals(this.fileAllowUpload, publicShareInfo.fileAllowUpload) &&
        Objects.equals(this.contentHidden, publicShareInfo.contentHidden) &&
        Objects.equals(this.mediaAllowDownload, publicShareInfo.mediaAllowDownload) &&
        Objects.equals(this.sharedBy, publicShareInfo.sharedBy) &&
        Objects.equals(this.partnerId, publicShareInfo.partnerId) &&
        Objects.equals(this.retailerId, publicShareInfo.retailerId) &&
        Objects.equals(this.authMethod, publicShareInfo.authMethod) &&
        Objects.equals(this.fileAllowOfficeOnline, publicShareInfo.fileAllowOfficeOnline) &&
        Objects.equals(this.fileAllowOfficeOnlineEdit, publicShareInfo.fileAllowOfficeOnlineEdit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, fileAllowUpload, contentHidden, mediaAllowDownload, sharedBy, partnerId, retailerId, authMethod, fileAllowOfficeOnline, fileAllowOfficeOnlineEdit);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PublicShareInfo {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    fileAllowUpload: ").append(toIndentedString(fileAllowUpload)).append("\n");
    sb.append("    contentHidden: ").append(toIndentedString(contentHidden)).append("\n");
    sb.append("    mediaAllowDownload: ").append(toIndentedString(mediaAllowDownload)).append("\n");
    sb.append("    sharedBy: ").append(toIndentedString(sharedBy)).append("\n");
    sb.append("    partnerId: ").append(toIndentedString(partnerId)).append("\n");
    sb.append("    retailerId: ").append(toIndentedString(retailerId)).append("\n");
    sb.append("    authMethod: ").append(toIndentedString(authMethod)).append("\n");
    sb.append("    fileAllowOfficeOnline: ").append(toIndentedString(fileAllowOfficeOnline)).append("\n");
    sb.append("    fileAllowOfficeOnlineEdit: ").append(toIndentedString(fileAllowOfficeOnlineEdit)).append("\n");
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

