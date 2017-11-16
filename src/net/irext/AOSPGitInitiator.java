package net.irext;

import net.irext.web.WebAPICallbacks;
import net.irext.web.WebAPIs;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Filename:       AOSPGitInitiator.java
 * Revised:        Date: 2017-11-13
 * Revision:       Revision: 1.0
 * <p>
 * Description:    AOSP Github Initiator
 * <p>
 * Revision log:
 * 2017-04-07: created by strawmanbobi
 */
public class AOSPGitInitiator {

    public static void main(String[] args) {
        try {
            System.out.println("=== AOSP Github Initiator ===");
            /*
             * The whole process contains 3 steps
             * 1. build default.xml for manifest project
             * 2. create sub-project repos
             * 3. TODO:
             */
            if (5 != args.length) {
                System.out.println("invalid parameter");
                System.out.println("Please call this method like java -jar AOSPGitInitiator.jar [token = 0] " +
                        "[source_repo_root] [dest_repo_root] [github base] [organization name]");
                return;
            }
            String githubToken = args[0];
            String sourceRoot = args[1];
            String destRoot = args[2];
            String githubBase = args[3];
            String orgName = args[4];

            System.out.println("  token : " + githubToken);
            System.out.println("  source : " + sourceRoot);
            System.out.println("  dest : " + destRoot);
            System.out.println("  github : " + githubBase);
            System.out.println("  org name : " + orgName);
            System.out.println("===============================");
            AOSPGitInitiator agi = new AOSPGitInitiator(githubToken, sourceRoot, destRoot,
                    githubBase, orgName);
            agi.createRepo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String MANIFEST_XML = "/.repo/manifests/default.xml";

    private static final String NODE_NAME_PROJECT = "project";
    private static final String NODE_NAME_REMOTE = "remote";
    private static final String NODE_NAME_DEFAULT = "default";

    private static final String NODE_REMOTE_ATTR_NAME = "name";
    private static final String NODE_REMOTE_ATTR_FETCH = "fetch";
    private static final String NODE_REMOTE_ATTR_REVIEW = "review";

    private static final String NODE_DEFAULT_ATTR_REVISION = "revision";
    private static final String NODE_DEFAULT_ATTR_REMOTE = "remote";
    private static final String NODE_DEFAULT_ATTR_SYNCJ = "sync-j";

    private static final String NODE_PROJECT_ATTR_PATH = "path";
    private static final String NODE_PROJECT_ATTR_NAME = "name";
    private static final String NODE_PROJECT_ATTR_GROUP = "group";

    private String token;
    private String sourceRoot;
    private String destRoot;
    private String githubOrgURL;
    private String githubBase;
    private String orgName;

    private AOSPGitInitiator(String token, String sourceRoot, String destRoot,
                             String githubBase, String orgName) {
        this.token = token;
        this.sourceRoot = sourceRoot;
        this.destRoot = destRoot;
        this.githubBase = githubBase;
        this.orgName = orgName;
        // ssh://git@adc.github.trendmicro.com/CoreTech-VMI-Unia/
        this.githubOrgURL = "ssh://git@" + githubBase + "/" + orgName + "/";
    }

    private void createRepo() {
        // step 1, build default.xml for manifest project
        System.out.println("Step 1. create target default.xml");

        String sourceManifestFilePath = sourceRoot + MANIFEST_XML;
        String destManifestFilePath = destRoot + MANIFEST_XML;
        InputStream sourceInput = null;

        try {
            Document doc = null;
            Element root = null;
            NodeList topNodes = null;
            List<String> projectList = new ArrayList<>();
            DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            sourceInput = new FileInputStream(sourceManifestFilePath);
            doc = domBuilder.parse(sourceInput);
            root = doc.getDocumentElement();
            topNodes = root.getChildNodes();

            int projectCount = 0;
            for (int topIndex = 0; topIndex < topNodes.getLength(); topIndex++) {
                Node keyItemNode = topNodes.item(topIndex);

                if (keyItemNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (keyItemNode.getNodeName().equals(NODE_NAME_REMOTE)) {
                        System.out.println("fetched element node of 'remote'");
                        // replace name and fetch attribute
                        NamedNodeMap attributes = keyItemNode.getAttributes();
                        for (int attriIndex = 0; attriIndex < attributes.getLength(); attriIndex++) {
                            Node item = attributes.item(attriIndex);
                            if (item.getNodeName().equals(NODE_REMOTE_ATTR_NAME)) {
                                System.out.println("replace remote->name to 'origin'");
                                item.setNodeValue("origin");
                            } else if (item.getNodeName().equals(NODE_REMOTE_ATTR_FETCH)) {
                                System.out.println("replace remote->fetch to custom github URL");
                                item.setNodeValue(this.githubOrgURL);
                            } else if (item.getNodeName().equals(NODE_REMOTE_ATTR_REVIEW)) {
                                System.out.println("remove this attribute");
                                item.setNodeValue("");
                            }
                        }
                    } else if (keyItemNode.getNodeName().equals(NODE_NAME_DEFAULT)) {
                        System.out.println("fetched element node of 'default'");
                        // replace name and fetch attribute
                        NamedNodeMap attributes = keyItemNode.getAttributes();
                        for (int attriIndex = 0; attriIndex < attributes.getLength(); attriIndex++) {
                            Node item = attributes.item(attriIndex);
                            if (item.getNodeName().equals(NODE_DEFAULT_ATTR_REVISION)) {
                                System.out.println("replace default->revision to 'master'");
                                item.setNodeValue("master");
                            } else if (item.getNodeName().equals(NODE_DEFAULT_ATTR_REMOTE)) {
                                System.out.println("replace default->remote to 'origin'");
                                item.setNodeValue("origin");
                            } else if (item.getNodeName().equals(NODE_DEFAULT_ATTR_SYNCJ)) {
                                System.out.println("replace default->sync-j to '4'");
                                item.setNodeValue("4");
                            }
                        }
                    } else if (keyItemNode.getNodeName().equals(NODE_NAME_PROJECT)) {
                        System.out.println("fetched element node of 'project'");
                        projectCount++;
                        NamedNodeMap attributes = keyItemNode.getAttributes();
                        for (int attriIndex = 0; attriIndex < attributes.getLength(); attriIndex++) {
                            Node item = attributes.item(attriIndex);
                            if (item.getNodeName().equals(NODE_PROJECT_ATTR_NAME)) {
                                String nameValue = item.getNodeValue();
                                String newNameValue = nameValue.replaceAll("/", "_");
                                projectList.add(newNameValue);
                                newNameValue += ".git";
                                item.setNodeValue(newNameValue);
                            }
                        }
                    } else {
                        System.out.println("fetched element node of '" + keyItemNode.getNodeName() + "'");
                    }
                }
            }

            // have some debug on project list
            for (String projectName : projectList) {
                System.out.println(projectName);
            }

            // generate dest default.xml
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(destManifestFilePath));
            transformer.transform(source, result);
            System.out.println("Step 1 done");

            // step 2. delete sub-projects under github organization
            // CreateRepoTask createRepoTask = new CreateRepoTask(projectList, githubBase, orgName);
            // createRepoTask.start();
            /*
            for (String projectName : ) {
                DeleteRepoTask deleteRepoTask = new DeleteRepoTask(projectName, githubBase, orgName);
                deleteRepoTask.start();
            }
            */

            // step 3. create sub-projects under github organization

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != sourceInput) {
                try {
                    sourceInput.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CreateRepoTask extends Thread {
        private List<String> projectList;
        private String githubBase;
        private String orgName;
        final private Object lock = new Object();

        CreateRepoTask(List<String> projectList, String githubBase, String orgName) {
            this.projectList = projectList;
            this.githubBase = githubBase;
            this.orgName = orgName;
        }

        @Override
        public void run() {
            for (String repoName : projectList) {
                try {
                    synchronized (lock) {
                        WebAPIs.getInstance().createRepo(repoName, repoName, githubBase, orgName, token,
                                new WebAPICallbacks.CreateRepoCallback() {
                                    @Override
                                    public void onCreateRepoSuccess(String response) {
                                        System.out.println("create repo successfully : " + repoName);
                                        // continue create an empty file to this repo
                                        WebAPIs.getInstance().createFile(repoName, githubBase, orgName, token,
                                                "empty", "init repo", "",
                                                new WebAPICallbacks.CreateFileCallback() {
                                                    @Override
                                                    public void onCreateFileSuccess(String response) {
                                                        System.out.println("create file to repo successfully : " +
                                                                repoName);
                                                    }

                                                    @Override
                                                    public void onCreateFileFailed() {
                                                        System.out.println("create file to repo " + repoName +
                                                                " failed");
                                                    }

                                                    @Override
                                                    public void onCreateFileError() {
                                                        System.out.println("create file to repo " + repoName +
                                                                " error");
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCreateRepoFailed() {
                                        System.out.println("create repo failed");
                                    }

                                    @Override
                                    public void onCreateRepoError() {
                                        System.out.println("create repo error");
                                    }
                                });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class DeleteRepoTask extends Thread {
        private String repoName;
        private String githubBase;
        private String orgName;

        public DeleteRepoTask(String repoName, String githubBase, String orgName) {
            this.repoName = repoName;
            this.githubBase = githubBase;
            this.orgName = orgName;
        }

        @Override
        public void run() {
            WebAPIs.getInstance().deleteRepo(repoName, githubBase, orgName, token,
                    new WebAPICallbacks.DeleteRepoCallback() {
                        @Override
                        public void onDeleteRepoSuccess(String response) {
                            System.out.println("delete repo successfully : " + response);
                        }

                        @Override
                        public void onDeleteRepoFailed() {
                            System.out.println("delete repo failed");
                        }

                        @Override
                        public void onDeleteRepoError() {
                            System.out.println("delete repo error");
                        }
                    });
        }
    }
}
