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
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
/**
 * List site full action history.
 */
@Schema(description = "List site full action history.")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")
public class ActionEntity {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("when")
  private DateTime when = null;

  @JsonProperty("destination")
  private String destination = null;

  @JsonProperty("display")
  private String display = null;

  @JsonProperty("ip")
  private String ip = null;

  @JsonProperty("source")
  private String source = null;

  @JsonProperty("targets")
  private List<String> targets = null;

  @JsonProperty("user_id")
  private Integer userId = null;

  @JsonProperty("username")
  private String username = null;

  /**
   * Type of action
   */
  public enum ActionEnum {
    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DESTROY("destroy"),
    MOVE("move"),
    LOGIN("login"),
    FAILEDLOGIN("failedlogin"),
    COPY("copy"),
    USER_CREATE("user_create"),
    USER_UPDATE("user_update"),
    USER_DESTROY("user_destroy"),
    GROUP_CREATE("group_create"),
    GROUP_UPDATE("group_update"),
    GROUP_DESTROY("group_destroy"),
    PERMISSION_CREATE("permission_create"),
    PERMISSION_DESTROY("permission_destroy"),
    API_KEY_CREATE("api_key_create"),
    API_KEY_UPDATE("api_key_update"),
    API_KEY_DESTROY("api_key_destroy");

    private String value;

    ActionEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static ActionEnum fromValue(String text) {
      for (ActionEnum b : ActionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("action")
  private ActionEnum action = null;

  /**
   * Failure type.  If action was a user login or session failure, why did it fail?
   */
  public enum FailureTypeEnum {
    EXPIRED_TRIAL("expired_trial"),
    ACCOUNT_OVERDUE("account_overdue"),
    LOCKED_OUT("locked_out"),
    IP_MISMATCH("ip_mismatch"),
    PASSWORD_MISMATCH("password_mismatch"),
    SITE_MISMATCH("site_mismatch"),
    USERNAME_NOT_FOUND("username_not_found"),
    NONE("none"),
    NO_FTP_PERMISSION("no_ftp_permission"),
    NO_WEB_PERMISSION("no_web_permission"),
    NO_DIRECTORY("no_directory"),
    ERRNO_ENOENT("errno_enoent"),
    NO_SFTP_PERMISSION("no_sftp_permission"),
    NO_DAV_PERMISSION("no_dav_permission"),
    NO_RESTAPI_PERMISSION("no_restapi_permission"),
    KEY_MISMATCH("key_mismatch"),
    REGION_MISMATCH("region_mismatch"),
    EXPIRED_ACCESS("expired_access"),
    DESKTOP_IP_MISMATCH("desktop_ip_mismatch"),
    DESKTOP_API_KEY_NOT_USED_QUICKLY_ENOUGH("desktop_api_key_not_used_quickly_enough"),
    DISABLED("disabled"),
    COUNTRY_MISMATCH("country_mismatch");

    private String value;

    FailureTypeEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static FailureTypeEnum fromValue(String text) {
      for (FailureTypeEnum b : FailureTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("failure_type")
  private FailureTypeEnum failureType = null;

  /**
   * Interface on which this action occurred.
   */
  public enum InterfaceEnum {
    WEB("web"),
    FTP("ftp"),
    ROBOT("robot"),
    JSAPI("jsapi"),
    WEBDESKTOPAPI("webdesktopapi"),
    SFTP("sftp"),
    DAV("dav"),
    DESKTOP("desktop"),
    RESTAPI("restapi"),
    SCIM("scim"),
    OFFICE("office");

    private String value;

    InterfaceEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static InterfaceEnum fromValue(String text) {
      for (InterfaceEnum b : InterfaceEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("interface")
  private InterfaceEnum _interface = null;

  public ActionEntity id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * Action ID
   * @return id
  **/
  @Schema(example = "1", description = "Action ID")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ActionEntity path(String path) {
    this.path = path;
    return this;
  }

   /**
   * Path
   * @return path
  **/
  @Schema(example = "path", description = "Path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public ActionEntity when(DateTime when) {
    this.when = when;
    return this;
  }

   /**
   * Action occurrence date/time
   * @return when
  **/
  @Schema(description = "Action occurrence date/time")
  public DateTime getWhen() {
    return when;
  }

  public void setWhen(DateTime when) {
    this.when = when;
  }

  public ActionEntity destination(String destination) {
    this.destination = destination;
    return this;
  }

   /**
   * The destination path for this action, if applicable
   * @return destination
  **/
  @Schema(example = "/to_path", description = "The destination path for this action, if applicable")
  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public ActionEntity display(String display) {
    this.display = display;
    return this;
  }

   /**
   * Friendly displayed output
   * @return display
  **/
  @Schema(example = "Actual text of the action here.", description = "Friendly displayed output")
  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }

  public ActionEntity ip(String ip) {
    this.ip = ip;
    return this;
  }

   /**
   * IP Address that performed this action
   * @return ip
  **/
  @Schema(example = "192.283.128.182", description = "IP Address that performed this action")
  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public ActionEntity source(String source) {
    this.source = source;
    return this;
  }

   /**
   * The source path for this action, if applicable
   * @return source
  **/
  @Schema(example = "/from_path", description = "The source path for this action, if applicable")
  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public ActionEntity targets(List<String> targets) {
    this.targets = targets;
    return this;
  }

  public ActionEntity addTargetsItem(String targetsItem) {
    if (this.targets == null) {
      this.targets = new ArrayList<>();
    }
    this.targets.add(targetsItem);
    return this;
  }

   /**
   * Targets
   * @return targets
  **/
  @Schema(example = "[]", description = "Targets")
  public List<String> getTargets() {
    return targets;
  }

  public void setTargets(List<String> targets) {
    this.targets = targets;
  }

  public ActionEntity userId(Integer userId) {
    this.userId = userId;
    return this;
  }

   /**
   * User ID
   * @return userId
  **/
  @Schema(example = "1", description = "User ID")
  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public ActionEntity username(String username) {
    this.username = username;
    return this;
  }

   /**
   * Username
   * @return username
  **/
  @Schema(example = "user", description = "Username")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public ActionEntity action(ActionEnum action) {
    this.action = action;
    return this;
  }

   /**
   * Type of action
   * @return action
  **/
  @Schema(example = "create", description = "Type of action")
  public ActionEnum getAction() {
    return action;
  }

  public void setAction(ActionEnum action) {
    this.action = action;
  }

  public ActionEntity failureType(FailureTypeEnum failureType) {
    this.failureType = failureType;
    return this;
  }

   /**
   * Failure type.  If action was a user login or session failure, why did it fail?
   * @return failureType
  **/
  @Schema(example = "none", description = "Failure type.  If action was a user login or session failure, why did it fail?")
  public FailureTypeEnum getFailureType() {
    return failureType;
  }

  public void setFailureType(FailureTypeEnum failureType) {
    this.failureType = failureType;
  }

  public ActionEntity _interface(InterfaceEnum _interface) {
    this._interface = _interface;
    return this;
  }

   /**
   * Interface on which this action occurred.
   * @return _interface
  **/
  @Schema(example = "web", description = "Interface on which this action occurred.")
  public InterfaceEnum getInterface() {
    return _interface;
  }

  public void setInterface(InterfaceEnum _interface) {
    this._interface = _interface;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActionEntity actionEntity = (ActionEntity) o;
    return Objects.equals(this.id, actionEntity.id) &&
        Objects.equals(this.path, actionEntity.path) &&
        Objects.equals(this.when, actionEntity.when) &&
        Objects.equals(this.destination, actionEntity.destination) &&
        Objects.equals(this.display, actionEntity.display) &&
        Objects.equals(this.ip, actionEntity.ip) &&
        Objects.equals(this.source, actionEntity.source) &&
        Objects.equals(this.targets, actionEntity.targets) &&
        Objects.equals(this.userId, actionEntity.userId) &&
        Objects.equals(this.username, actionEntity.username) &&
        Objects.equals(this.action, actionEntity.action) &&
        Objects.equals(this.failureType, actionEntity.failureType) &&
        Objects.equals(this._interface, actionEntity._interface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, path, when, destination, display, ip, source, targets, userId, username, action, failureType, _interface);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActionEntity {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    when: ").append(toIndentedString(when)).append("\n");
    sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
    sb.append("    display: ").append(toIndentedString(display)).append("\n");
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    targets: ").append(toIndentedString(targets)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    failureType: ").append(toIndentedString(failureType)).append("\n");
    sb.append("    _interface: ").append(toIndentedString(_interface)).append("\n");
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
