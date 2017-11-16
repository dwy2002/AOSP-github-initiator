package net.irext.web.request;

/**
 * Filename:       CreateRepoRequest.java
 * Revised:        Date: 2017-11-13
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Create Github Repo
 * <p>
 * Revision log:
 * 2017-11-13: created by strawmanbobi
 */
public class CreateRepoRequest extends BaseRequest {

    String name;
    String description;

    public CreateRepoRequest(String token, String name) {
        super(token);
        this.name = name;
    }

    public CreateRepoRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
