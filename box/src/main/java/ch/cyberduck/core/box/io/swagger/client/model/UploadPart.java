/*
 * Box Platform API
 * [Box Platform](https://box.dev) provides functionality to provide access to content stored within [Box](https://box.com). It provides endpoints for basic manipulation of files and folders, management of users within an enterprise, as well as more complex topics such as legal holds and retention policies.
 *
 * OpenAPI spec version: 2.0.0
 * Contact: devrel@box.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.cyberduck.core.box.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The representation of an upload session chunk.
 */
@Schema(description = "The representation of an upload session chunk.")


public class UploadPart {
    @JsonProperty("part_id")
    private String partId = null;

    @JsonProperty("offset")
    private Long offset = null;

    @JsonProperty("size")
    private Long size = null;

    @JsonProperty("sha1")
    private String sha1 = null;

    public UploadPart partId(String partId) {
        this.partId = partId;
        return this;
    }

    /**
     * The unique ID of the chunk.
     *
     * @return partId
     **/
    @Schema(example = "6F2D3486", description = "The unique ID of the chunk.")
    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public UploadPart offset(Long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * The offset of the chunk within the file in bytes. The lower bound of the position of the chunk within the file.
     *
     * @return offset
     **/
    @Schema(example = "16777216", description = "The offset of the chunk within the file in bytes. The lower bound of the position of the chunk within the file.")
    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public UploadPart size(Long size) {
        this.size = size;
        return this;
    }

    /**
     * The size of the chunk in bytes.
     *
     * @return size
     **/
    @Schema(example = "3222784", description = "The size of the chunk in bytes.")
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public UploadPart sha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    /**
     * The SHA1 hash of the chunk.
     *
     * @return sha1
     **/
    @Schema(example = "134b65991ed521fcfe4724b7d814ab8ded5185dc", description = "The SHA1 hash of the chunk.")
    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadPart uploadPart = (UploadPart) o;
        return Objects.equals(this.partId, uploadPart.partId) &&
                Objects.equals(this.offset, uploadPart.offset) &&
                Objects.equals(this.size, uploadPart.size) &&
                Objects.equals(this.sha1, uploadPart.sha1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partId, offset, size, sha1);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UploadPart {\n");

        sb.append("    partId: ").append(toIndentedString(partId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    sha1: ").append(toIndentedString(sha1)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if(o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
