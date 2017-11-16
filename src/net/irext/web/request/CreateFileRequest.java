package net.irext.web.request;

/**
 * Filename:       CreateFileRequest.java
 * Revised:        Date: 2017-11-15
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Create a File to certain repo
 * <p>
 * Revision log:
 * 2017-11-15: created by strawmanbobi
 */
public class CreateFileRequest extends BaseRequest {

    String repoName;
    String filePath;
    String message;
    String content;

    public CreateFileRequest(String token, String repoName, String filePath, String message, String content) {
        super(token);
        this.repoName = repoName;
        this.filePath = filePath;
        this.message = message;
        this.content = content;
    }

    public CreateFileRequest() {
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
