# IMPORTANT NODE

I'm in the process of heavily reworking both libs and multisrc to adapt them to this repo,
if you don't plan on using them, everything should be ready.

# Fast Guide

Both guides explain all the steps needed, but this version should be used if you're confident
in your knowledge and/or to set things up quickly.
You can switch back and forth between the fast and in-depth guides, so if you're curious about
something you can check the corresponding step in the
[In-Dept Guide](#in-dept-guide), which will provide more information.

1. [[ID1](#step-1)] Either fork or use this repository as a template.
   (both have their own disadvantages, if you don't know the difference check the in-depth step.)
2. [[ID2](#step-2)] Use the task `:new:extension --identifier <id>` to create a skeletal extension
   in [extensions](./extensions). Remember to reload the gradle project.
3. [[ID3](#step-3)] Modify the build.gradle.kts to your liking through
   setupTachiyomiExtensionConfiguration
    - You can add libs and multisrc through the configuration function
4. [[ID4](#step-4)] Create the necessary icons (ic_launcher.png) through whatever
   means ([recommended tool](https://as280093.github.io/AndroidAssetStudio/icons-launcher.html)).
5. [[ID5](#step-5)] In the generated package, create a new class that extends HttpSource or
   ParsedHttpSource
    - Remember to rename the package
    - I **might** create a class to make the process easier in the future
6. [[ID6](#step-6)] Implement your source logic how you would've in the original extension repo
    - Remember to modify the `AndroidManifest.xml` file if you plan on using activities.
7. [[ID7](#step-7)] Run the command `:constructDebugRepo`, if everything goes well, a fully built
   debug repository should be under /build/repo/debug
    - You can serve this directory to `127.0.0.1:8080` using the `:serve:debugRepo` task and access
      it from the emulated device with `10.0.2.2:8080`
8. [[ID8](#step-8)] If everything works as necessary, you will need to setup some stuff on GitHub:
    1. Secrets:
        - KEY_FILE_NAME: file name where the keystore will be temporarily decoded, you can use
          an [UUID](https://www.uuidgenerator.net/) (only for this)
        - KEY_STORE: base64 encoded version of the `.keystore` generated with `keytool`
        - KEY_STORE_ALIAS: alias for the key provided at time of creation of the `.keystore`
        - KEY_PASSWORD, KEY_STORE_PASSWORD: depending on your means for generating the `.keystore`
          they might be the same or different. Provided at the time of creation of the `.keystore`
    2. Variables:
        - DO_PUBLISH_REPO: if "true", the github workflow will use the `repo` branch as the
          release repository location. You can use `git switch --orphan repo`
          and `git commit --allow-empty -m "Empty repo"` to create a branch with no history
          (be careful about uncommitted changes to the `master` branch).
9. [[ID9](#step-9)] By default the workflow will only run when you request it from GitHub, you can
   change this by modifying [build_release.main.kts](.github/workflows/build_release.main.kts), but
   this is not recommended.
10. [[ID10](#step-10)] If everything went well you should have a `repo` branch containing all the
    necessary data.

# In-Dept Guide

<a id="pookie"></a>

## Step 1

[Back to fast](#fast-guide)

You can either fork this repository, or use it as a template,
citing [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template):

> Creating a repository from a template is similar to forking a repository, but there are important
> differences:
>
> - A new fork includes the entire commit history of the parent repository, while a repository
    created from a template starts with a single commit.
>
> - Commits to a fork don't appear in your contributions graph, while commits to a repository
    created from a template do appear in your contribution graph.
>
> - A fork can be a temporary way to contribute code to an existing project, while creating a
    repository from a template starts a new project quickly.

A template should be the recommended way to go, but unfortunately that also means you won't be able
to easily update your repository when this template gets updated.
[There are workarounds](https://stackoverflow.com/a/56577320), but they aren't pretty.

Furthermore, GitHub limits fork counts for a single individual
(you can create an organization) to one per unique repository,
so if you wanted to create a pull request to this repository
you would need to do some git gymnastics.

Ideally, using this template, you shouldn't need to touch anything outside
of [extensions](./extensions), if you find that need,
you most likely found a missing feature and a nice opportunity for a PR.
As such, updating could be as easy as just
saving that directory and replacing everything around it.

The ultimate choice is yours, depending on if you plan on contributing
to this repository.
You can read more about how to work with forks and
PRs [here](https://gist.github.com/Chaser324/ce0505fbed06b947d962).

## Step 2

[Back to fast](#fast-guide)

**WIP**

## Step 3

[Back to fast](#fast-guide)

**WIP**

## Step 4

[Back to fast](#fast-guide)

**WIP**

## Step 5

[Back to fast](#fast-guide)

**WIP**

## Step 6

[Back to fast](#fast-guide)

**WIP**

## Step 7

[Back to fast](#fast-guide)

**WIP**

## Step 8

[Back to fast](#fast-guide)

**WIP**

## Step 9

[Back to fast](#fast-guide)

**WIP**

## Step 10

[Back to fast](#fast-guide)

**WIP**
