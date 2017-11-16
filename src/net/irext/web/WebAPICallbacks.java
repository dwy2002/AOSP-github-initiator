package net.irext.web;

/**
 * Filename:       WebAPICallbacks.java
 * Revised:        Date: 2017-11-13
 * Revision:       Revision: 1.0
 * <p>
 * Description:    HTTP Response Callbacks
 * <p>
 * Revision log:
 * 2017-11-13: created by strawmanbobi
 */
public class WebAPICallbacks {

    public interface CreateRepoCallback {
        void onCreateRepoSuccess(String response);
        void onCreateRepoFailed();
        void onCreateRepoError();
    }

    public interface DeleteRepoCallback {
        void onDeleteRepoSuccess(String response);
        void onDeleteRepoFailed();
        void onDeleteRepoError();
    }

    public interface CreateFileCallback {
        void onCreateFileSuccess(String response);
        void onCreateFileFailed();
        void onCreateFileError();
    }
}
