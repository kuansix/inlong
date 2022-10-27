package org.apache.inlong.sort.doris.table.models;

import org.apache.doris.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.doris.shaded.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.doris.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.doris.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespContent {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty(value = "TxnId")
    private int txnId;

    @JsonProperty(value = "Label")
    private String label;

    @JsonProperty(value = "Status")
    private String status;

    @JsonProperty(value = "ExistingJobStatus")
    private String existingJobStatus;

    @JsonProperty(value = "Message")
    private String message;

    @JsonProperty(value = "NumberTotalRows")
    private long numberTotalRows;

    @JsonProperty(value = "NumberLoadedRows")
    private long numberLoadedRows;

    @JsonProperty(value = "NumberFilteredRows")
    private int numberFilteredRows;

    @JsonProperty(value = "NumberUnselectedRows")
    private int numberUnselectedRows;

    @JsonProperty(value = "LoadBytes")
    private long loadBytes;

    @JsonProperty(value = "LoadTimeMs")
    private int loadTimeMs;

    @JsonProperty(value = "BeginTxnTimeMs")
    private int beginTxnTimeMs;

    @JsonProperty(value = "StreamLoadPutTimeMs")
    private int streamLoadPutTimeMs;

    @JsonProperty(value = "ReadDataTimeMs")
    private int readDataTimeMs;

    @JsonProperty(value = "WriteDataTimeMs")
    private int writeDataTimeMs;

    @JsonProperty(value = "CommitAndPublishTimeMs")
    private int commitAndPublishTimeMs;

    @JsonProperty(value = "ErrorURL")
    private String errorURL;

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
