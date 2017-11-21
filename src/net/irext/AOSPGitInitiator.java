package net.irext;

import com.nikhaldimann.inieditor.IniEditor;
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
import java.util.Scanner;

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
            if (6 != args.length) {
                System.out.println("invalid parameter");
                System.out.println("Please call this method like java -jar AOSPGitInitiator.jar [action] [token] " +
                        "[source_repo_root] [dest_repo_root] [github base] [organization name]");
                return;
            }
            String action = args[0];
            String githubToken = args[1];
            String sourceRoot = args[2];
            String destRoot = args[3];
            String githubBase = args[4];
            String orgName = args[5];

            System.out.println("  action : " + action);
            System.out.println("  token : " + githubToken);
            System.out.println("  source : " + sourceRoot);
            System.out.println("  dest : " + destRoot);
            System.out.println("  github : " + githubBase);
            System.out.println("  org name : " + orgName);
            System.out.println("===============================");
            System.out.println();
            System.out.println("Please check the parameters and type 'YES' to continue");
            Scanner input = new Scanner(System.in);
            if (input.next().equalsIgnoreCase("YES")) {
                AOSPGitInitiator agi = new AOSPGitInitiator(githubToken, sourceRoot, destRoot,
                        githubBase, orgName);
                if (action.equals("add")) {
                    agi.createRepo();
                } else if (action.equals("delete")) {
                    agi.deleteRepo();
                } else if (action.equals("update-remote")) {
                    agi.updateRemote();
                } else {
                    System.out.println("Invalid action : " + action);
                }
            } else {
                System.out.println("Bye");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String MANIFEST_PATH = "/.repo/manifests/";
    private static final String MANIFEST_XML = "/.repo/manifests/default.xml";

    private static final String NODE_NAME_PROJECT = "project";
    private static final String NODE_NAME_REMOTE = "remote";
    private static final String NODE_NAME_DEFAULT = "default";
    private static final String NODE_NAME_REPO_HOOKS = "repo-hooks";

    private static final String NODE_REMOTE_ATTR_NAME = "name";
    private static final String NODE_REMOTE_ATTR_FETCH = "fetch";
    private static final String NODE_REMOTE_ATTR_REVIEW = "review";

    private static final String NODE_DEFAULT_ATTR_REVISION = "revision";
    private static final String NODE_DEFAULT_ATTR_REMOTE = "remote";
    private static final String NODE_DEFAULT_ATTR_SYNCJ = "sync-j";

    private static final String NODE_PROJECT_ATTR_PATH = "path";
    private static final String NODE_PROJECT_ATTR_NAME = "name";
    private static final String NODE_PROJECT_ATTR_GROUP = "group";

    private static final String NODE_REPO_HOOKS_IN_PROJECT = "in-project";

    private static final String CONFIG_REMOTE_ORIGIN = "remote \"origin\"";
    private static final String CONFIG_CORE = "core";

    private static final String CONFIG_REMOTE_URL = "url";
    private static final String CONFIG_REMOTE_REVIEW = "review";
    private static final String CONFIG_REMOTE_PROJECT_NAME = "projectname";
    private static final String CONFIG_REMOTE_FETCH = "fetch";

    private static final String CONFIG_FETCH = "+refs/heads/*:refs/remotes/origin/*";

    private static final String CONFIG_CORE_FILEMODE = "filemode";


    private String action;
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
        // ssh://[githubBase]/[orgName]/
        this.githubOrgURL = "ssh://git@" + githubBase + "/" + orgName + "/";
    }


    private void createRepo() {
        System.out.println("=== create repos ===");
        Document doc = null;
        Element root = null;
        NodeList topNodes = null;
        InputStream sourceInput = null;

        String sourceManifestFilePath = sourceRoot + MANIFEST_XML;
        String destManifestFilePath = destRoot + MANIFEST_XML;
        String destManifestFolder = destRoot + MANIFEST_PATH;

        try {
            // step 1, build default.xml for manifest project
            File destFolder = new File(destManifestFolder);
            destFolder.mkdirs();

            DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            sourceInput = new FileInputStream(sourceManifestFilePath);
            doc = domBuilder.parse(sourceInput);
            root = doc.getDocumentElement();
            topNodes = root.getChildNodes();
            List<String> projectList = new ArrayList<>();

            int projectCount = 0;
            for (int topIndex = 0; topIndex < topNodes.getLength(); topIndex++) {
                Node keyItemNode = topNodes.item(topIndex);

                if (keyItemNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (keyItemNode.getNodeName().equals(NODE_NAME_REMOTE)) {
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
                    } else if (keyItemNode.getNodeName().equals(NODE_NAME_REPO_HOOKS)) {
                        // update attribute of repo-hooks
                        NamedNodeMap attributes = keyItemNode.getAttributes();
                        for (int attriIndex = 0; attriIndex < attributes.getLength(); attriIndex++) {
                            Node item = attributes.item(attriIndex);
                            if (item.getNodeName().equals(NODE_REPO_HOOKS_IN_PROJECT)) {
                                System.out.println("modify repo-hooks->in-project");
                                String nameValue = item.getNodeValue();
                                String newNameValue = nameValue.replaceAll("/", "_");
                                newNameValue += ".git";
                                item.setNodeValue(newNameValue);
                            }
                        }
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
            System.out.println("target default.xml generated");

            // step 2. delete sub-projects under github organization
            CreateRepoTask createRepoTask = new CreateRepoTask(projectList, githubBase, orgName);
            createRepoTask.start();
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

    private void deleteRepo() {
        System.out.println("=== delete repos ===");

        Document doc = null;
        Element root = null;
        NodeList topNodes = null;
        InputStream sourceInput = null;

        String sourceManifestFilePath = sourceRoot + MANIFEST_XML;

        List<String> projectList = new ArrayList<>();

        try {
            DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            sourceInput = new FileInputStream(sourceManifestFilePath);
            doc = domBuilder.parse(sourceInput);
            root = doc.getDocumentElement();
            topNodes = root.getChildNodes();
            for (int topIndex = 0; topIndex < topNodes.getLength(); topIndex++) {
                Node keyItemNode = topNodes.item(topIndex);

                if (keyItemNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (keyItemNode.getNodeName().equals(NODE_NAME_PROJECT)) {
                        NamedNodeMap attributes = keyItemNode.getAttributes();
                        for (int attriIndex = 0; attriIndex < attributes.getLength(); attriIndex++) {
                            Node item = attributes.item(attriIndex);
                            if (item.getNodeName().equals(NODE_PROJECT_ATTR_NAME)) {
                                String nameValue = item.getNodeValue();
                                String newNameValue = nameValue.replaceAll("/", "_");
                                projectList.add(newNameValue);
                            }
                        }
                    }
                }
            }
            DeleteRepoTask deleteRepoTask = new DeleteRepoTask(projectList, githubBase, orgName);
            deleteRepoTask.start();
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

    private void updateRemote() {
        System.out.println("=== update remote for repos ===");

        Document doc = null;
        Element root = null;
        NodeList topNodes = null;
        InputStream sourceInput = null;

        String sourceManifestFilePath = sourceRoot + MANIFEST_XML;
        String sourceRootPath = sourceRoot;

        List<ProjectInfo> projectList = new ArrayList<>();

        try {
            DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            sourceInput = new FileInputStream(sourceManifestFilePath);
            doc = domBuilder.parse(sourceInput);
            root = doc.getDocumentElement();
            topNodes = root.getChildNodes();
            for (int topIndex = 0; topIndex < topNodes.getLength(); topIndex++) {
                Node keyItemNode = topNodes.item(topIndex);

                if (keyItemNode.getNodeType() == Node.ELEMENT_NODE &&
                    keyItemNode.getNodeName().equals(NODE_NAME_PROJECT)) {
                    NamedNodeMap attributes = keyItemNode.getAttributes();
                    ProjectInfo pi = new ProjectInfo();
                    for (int attriIndex = 0; attriIndex < attributes.getLength(); attriIndex++) {
                        Node item = attributes.item(attriIndex);
                        if (item.getNodeName().equals(NODE_PROJECT_ATTR_NAME)) {
                            String nameValue = item.getNodeValue();
                            String newNameValue = nameValue.replaceAll("/", "_");
                            pi.setProjectName(newNameValue);
                        } else if (item.getNodeName().equals(NODE_PROJECT_ATTR_PATH)) {
                            String pathValue = item.getNodeValue();
                            pi.setPath(pathValue);
                        }
                    }
                    projectList.add(pi);
                }
            }
            UpdateRemoteTask updateRemoteTask = new UpdateRemoteTask(sourceRoot, projectList);
            updateRemoteTask.start();

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

    // helper threads
    private class CreateRepoTask extends Thread {
        private List<String> projectList;
        private String githubBase;
        private String orgName;
        final private Object lock = new Object();
        private int createdProject;
        private int totalProjectCount;

        CreateRepoTask(List<String> projectList, String githubBase, String orgName) {
            this.projectList = projectList;
            this.githubBase = githubBase;
            this.orgName = orgName;
            this.createdProject = 0;
            this.totalProjectCount = this.projectList.size();
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
                                                                repoName + " (" + ++createdProject + "/" +
                                                                totalProjectCount + ")");
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
                                        System.out.println("create repo failed : " + repoName);
                                    }

                                    @Override
                                    public void onCreateRepoError() {
                                        System.out.println("create repo error : " + repoName);
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
        private List<String> projectList;
        private String githubBase;
        private String orgName;
        private int deletedProject;
        private int totalProjectCount;

        DeleteRepoTask(List<String> projectList, String githubBase, String orgName) {
            this.projectList = projectList;
            this.githubBase = githubBase;
            this.orgName = orgName;
            this.deletedProject = 0;
            this.totalProjectCount = projectList.size();
        }

        @Override
        public void run() {
            for (String repoName : projectList) {
                WebAPIs.getInstance().deleteRepo(repoName, githubBase, orgName, token,
                        new WebAPICallbacks.DeleteRepoCallback() {
                            @Override
                            public void onDeleteRepoSuccess(String response) {
                                System.out.println("delete repo successfully : " + repoName + " (" + ++deletedProject + "/" +
                                        totalProjectCount + ")");
                            }

                            @Override
                            public void onDeleteRepoFailed() {
                                System.out.println("delete repo failed : " + repoName);
                            }

                            @Override
                            public void onDeleteRepoError() {
                                System.out.println("delete repo error : " + repoName);
                            }
                        });
            }
        }
    }

    private class UpdateRemoteTask extends Thread {
        private String sourceRoot;
        private List<ProjectInfo> projectList;

        UpdateRemoteTask(String sourceRoot, List<ProjectInfo> projectList) {
            this.sourceRoot = sourceRoot;
            this.projectList = projectList;
        }

        @Override
        public void run() {
            try {
                int iniProcessed = 0;
                for (ProjectInfo pi : projectList) {
                    String targetFile = sourceRoot + "/" + pi.getPath() + "/.git/config";
                    File gitConfigFile = new File(targetFile);
                    if (!gitConfigFile.exists()) {
                        System.out.println("validate file failed : " + gitConfigFile);
                    } else {
                        // read config file in ini format
                        IniEditor gitConfig = new IniEditor();
                        gitConfig.load(targetFile);
                        gitConfig.addSection("remote \"origin\"");
                        gitConfig.set(CONFIG_REMOTE_ORIGIN,
                                CONFIG_REMOTE_URL,
                                "git@adc.github.trendmicro.com:CoreTech-VMI-Unia/" + pi.getProjectName() + ".git");
                        gitConfig.set(CONFIG_REMOTE_ORIGIN,
                                CONFIG_REMOTE_REVIEW,
                                "");
                        gitConfig.set(CONFIG_REMOTE_ORIGIN,
                                CONFIG_REMOTE_PROJECT_NAME,
                                pi.getProjectName());
                        gitConfig.set(CONFIG_REMOTE_ORIGIN,
                                CONFIG_REMOTE_FETCH,
                                CONFIG_FETCH);

                        // update file mode as well
                        gitConfig.set(CONFIG_CORE,
                                CONFIG_CORE_FILEMODE,
                                "false");
                        iniProcessed++;
                        gitConfig.save(targetFile);
                        System.out.println("git remote config done : " + gitConfigFile +
                                ", (" + iniProcessed + "/" + projectList.size() + ")");
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    // helper bean
    private class ProjectInfo {
        private String path;
        private String projectName;

        ProjectInfo(String path, String projectName) {
            this.path = path;
            this.projectName = projectName;
        }

        ProjectInfo () {

        }

        String getPath() {
            return path;
        }

        void setPath(String path) {
            this.path = path;
        }

        public String getProjectName() {
            return projectName;
        }

        void setProjectName(String projectName) {
            this.projectName = projectName;
        }
    }
}
