# AOSP-github-initiator
This tools helps building your own AOSP organization and porting all source code of AOSP to it

### Prerequisites
1. Create a mirror of Google AOSP and repo init+sync to your local machine, refer to https://source.android.com/source/downloading for details. In this case, we sync the source code to the local folder eg. /home/git/aosp
2. Set up your own github(EE) organization, named eg. MyAOSP
3. Set up SSH-Key of your github account
4. Generate github personal access token
5. Set up Java runtime

### Command format

```
java -jar AOSP-github-initiator.jar [action] [access token] [src_path] [dest_path] [githubBase] [organizationName]
```

Parameter        | Description
---------------- | -------------
action           | add / delete / update-remote
access token     | github access token
src_path         | the local AOSP root path in prerequisites step 1, eg. /home/git/aosp
dest_path        | a temporary repo root path, eg. /home/git/myAosp
githubBase       | your github(EE) URL base, eg. github.com
organizationName | organization name which will hold all AOSP projects, eg. my-aosp

* About action
    * add - create all projects for your organization according to .repo/manifests/default.xml in src_path and output a new default.xml in .repo/manifests/ in dest_path
    * delete - delete all projects from your organization according to .repo/manifests/default.xml in src_path
    * update-remote - add a new remote of your created projects in src_path pointing to the projects in your organization

### How to use
1. Do repo init+sync for Google official AOSP (refer to prerequisites, step 1)
2. Run **java -jar AOSP-github-initiator.jar delete ...** (not necessary for the first time)
3. Run **java -jar AOSP-github-initiator.jar add ...** (after this step, you could see all the projects are created in your organization and a new default.xml is generated in dest_path/.repo/manifests/)
4. Run **java -jar AOSP-github-initiator.jar update-remote...** (after this step, you could see there is a new remote tag is created in any of your Android project, eg. build/make/.git/config

```
[core]
repositoryformatversion = 0
filemode = false
[filter "lfs"]
smudge = git-lfs smudge --skip -- %f
[remote "aosp"]
url = URL to AOSP repo
review = https://android-review.googlesource.com/
projectname = platform/build
fetch = +refs/heads/*:refs/remotes/aosp/*
[remote "origin"]
url = URL to your own repo
review = 
projectname = platform_build
fetch = +refs/heads/*:refs/remotes/origin/*
```

5. CD to your AOSP root path, With repo batch commands, you can complete the left work

```
$ cd [src_path]
$ repo forall -c git checkout 'some AOSP branch' -b 'your own branch name'
$ repo forall -c git push origin 'your own branch name'
```

Contact me for any questions
* Email: strawmanbobi@163.com
* Wechat: strawmanbobi
