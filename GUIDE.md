# IMPORTANT NODE

I'm in the process of heavily reworking both libs and multisrc to adapt them to this repo,
if you don't plan on using them, everything should be ready.

## FYIs

I recommend **NOT** using the Android project view (top left), as it can get quite uncomfortable.
Unfortunately Android Studio really likes to switch to it unexpectedly.

# Guides

Both setup guides explain all the steps needed, the fast version can be used if you're confident
in your knowledge, have already done this and need a refresher, or to set things up quickly.

You can switch back and forth between the fast and in-depth guides by clicking on the `[ID*]` links,
so if you're curious about something you can just check the corresponding step,
which will provide more information.

## Prerequisites

You should optimally have a JDK 17 or 21 installed, I personally recommend
[Adoptium](https://adoptium.net/) but any decent JDK version 11 or later should work.
The workflow runs on Java 21.

## Fast Setup

1. [[ID1](#step-1)] Use this repository as a template.
2. [[ID2](#step-2-4)] Use the task `:new:extension --identifier <id>` to create a skeletal extension
   in [extensions](./extensions). Remember to reload the gradle project.
3. [[ID3](#step-2-4)] Modify the build.gradle.kts to your liking through
   `setupTachiyomiExtensionConfiguration`
    - You can add libs and multisrc through the configuration function
4. [[ID4](#step-2-4)] In the generated package, create a new class that extends HttpSource or
   ParsedHttpSource
    - Remember to rename the package
    - I **might** create a class to make the process easier in the future
5. [[ID5](#step-5)] Create the necessary icons (ic_launcher.png) through whatever
   means ([recommended tool](https://as280093.github.io/AndroidAssetStudio/icons-launcher.html)).
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
10. If everything went well you should have a `repo` branch containing all the
    necessary data.

## In-Dept Setup

Some assumptions are made in this guide:

- You know what gradle tasks are and how to execute them.
- You know have a moderate grasp over the [Kotlin](https://kotlinlang.org/) language.

### Step 1

[Back to fast](#fast-setup)

Citing [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template),
the difference between creating a fork and using a template is:

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

I'll work on facilitating the process of updating the template using releases,
hopefully requiring the least amount of work possible.

The core idea is that, ideally, using this template,
you shouldn't need to touch any build logic or convention
outside of the [extensions](./extensions) directory.
If you did find the need to modify the build logic, you most likely found a missing feature and a
nice opportunity for a PR.

I'll probably create a workflow to strip down
the template to its bare-bone essentials to avoid replacing any
files like `.editorconfig` and such.

-----

### Step 2-4

[Back to fast](#fast-setup)

You can use the `:new:extension --identifier <id>` gradle task, to create
a new directory named `<id>` under [extensions](./extensions) (the task will fail if the directory
already exists). You can inspect the task by checking
the [build.gradle.kts](./utils/new/build.gradle.kts) under `utils/new`.

Remember to reload the gradle project to see the new extension.

A couple of things will be set up for you:

- A minimal `AndroidManifest.xml`, which is needed by the android build process. You will need to
  modify this if you wish to provide some advanced functionalities to your app, like url
  activities, but for a minimal extension, you won't need to touch it.
- A `build.gradle.kts` which will contain a call to `setupTachiyomiExtensionConfiguration`, defined
  in the [extension.kt](./build-src/conventions/src/main/kotlin/extension.kt) convention. You can
  read the documentation of the function to understand what every parameter does, but primarily you
  will need to change these ones:
    1. `namespaceIdentifier`: This represents an identifier to group all your extensions together.
       I recommend using the same package prefix you would use when creating a JVM/Kotlin
       library/app. (e.g. for me `dev.npgx.etc` => `npgx`)
    2. `extName`: This is the name of the extension that will appear to the user, both in the app
       name and in the extensions list under tachiyomi.
    3. `pkgNameSuffix`: Convention is to use a lowercase version of the `extName`.
       (e.g. `A Pair of 2+` => `apairof2plus`)
    4. `extClass`: Name of the class that represents your extension, it can be anything, but should
       represent your extension. (e.g. `A Pair of 2+` => `.APairOf2Plus`)
       You can use the `Factory` suffix if the class represents an extension factory.
       (multiple sources)
       **NOTE:** The `.` is needed and intentional, it is an artifact of the way tachiyomi handles
       extensions.
    5. `extVersionCode`: Represents the version of your extension, should be increased every time
       you want your users to update the extension (otherwise they won't be prompted with an update)
    6. `isNsfw`: Whether or not the extension manages Not Safe For Work content.

The task automatically creates the `src` and `res` directories and a `eu.kanade.tachiyomi.extension`
empty package.

Create a class under `eu.kanade.tachiyomi.extension.{namespaceIdentifier}.{pkgNameSuffix}`
named `{extClass}` (without the dot).

So for example, if I wanted to create an extension called `Project Suki` I would configure the
extension as such:

- `namespaceIdentifier`: `npgx`
- `extName`: `Project Suki`
- `pkgNameSuffix`: `projectsuki`
- `extClass`: `.ProjectSuki`

I would then create a class called `ProjectSuki`
under `src/eu/kanade/tachiyomi/extension/npgx/projectsuki`.

The class should extend:

- `HttpSource` if your source doesn't use a standard template structure,
  (like being hand-made through a CSS library such as [Bootstrap](https://getboostrap.com)).
- `ParsedHttpSource` if your source does follow a template, but this template isn't
  [one of the known (`MultiSrc` class)](./build-src/conventions/src/main/kotlin/extension.kt)
  templates.
- One of the multisrc templates
  like [Madara](./multisrc/madara/src/main/kotlin/eu/kanade/tachiyomi/multisrc/madara/Madara.kt)
  if your source uses that. You can include the multisrc
  logic into your extension by simply adding a `multisrc` parameter to
  the `setupTachiyomiExtensionConfiguration` function.

You can also add
[libraries (`TachiyomiLibrary` class)](./build-src/conventions/src/main/kotlin/extension.kt)
via the `libs` `setupTachiyomiExtensionConfiguration` parameter.

-----

### Step 5

[Back to fast](#fast-setup)

Icons need to be created to follow this structure:

```doxygen
  res
  ├── mipmap-hdpi
  │   └── ic_launcher.png
  ├── mipmap-mdpi
  │   └── ic_launcher.png
  ├── mipmap-xhdpi
  │   └── ic_launcher.png
  ├── mipmap-xxhdpi
  │   └── ic_launcher.png
  └── mipmap-xxxhdpi
      └── ic_launcher.png
```

You can use the
[recommended tool](https://as280093.github.io/AndroidAssetStudio/icons-launcher.html)
to do this, but this isn't strictly necessary.

Your icon should be somewhat representing of your extension.

If you don't provide any icon, the default:

![the default](./utils/default/res/mipmap-hdpi/ic_launcher.png)

will be used.

-----

### Step 6

[Back to fast](#fast-setup)

**WIP**

-----

### Step 7

[Back to fast](#fast-setup)

Tachiyomi expects sources repo to follow this structure:

```doxygen
  apk
  ├── <ext1>.apk
  ├── <ext2>.apk
  └── <ext3>.apk
  
  icon
  ├── <ext1>.png
  ├── <ext1-package>.png
  ├── <ext2>.png
  ├── <ext2-package>.png
  ├── <ext3>.png
  └── <ext3-package>.png
  
  index.min.json

```

The provided `:construct<variant>Repo` tasks will do everything for you,
you can use the `:constructDebugRepo` to create local debug APKs.

I do not recommend providing release signing variables and secrets
on your local machine unless you have experience with this whole process.
The `debug` variants should suffice for the majority of cases and mirror
`release` variants exactly.

You will find them under the `<project root>/build/repo` directory.

**NOTE:** as of now, tachiyomi doesn't allow for non-https urls (such as localhost),
and everything is locked down (Issues, PRs, Discord), so the following steps are for future
reference.

There is also a built-in `:serve:debugRepo` task that provides a
static server using [Ktor](https://ktor.io) to serve the `build/repo/debug`
directory over `127.0.0.1:8080`.

You can access it through `10.0.2.2:8080` in the emulator of your choosing to test your app.

-----

### Step 8

[Back to fast](#fast-setup)

If the debug repo and all your extensions work as intended, you can move onto the release variant:

You'll first need to either use or create a keystore.
To create a keystore, you'll need to use
the [keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)
executable, which should come bundled with most JDKs.

Prepare a new directory wherever you like (for example `~/secrets`),
create a new file `pass` and put inside of it the password you want to use
(with recent versions of keytool, both passwords will be forced to be the same, so only one is
needed).

Finally, execute the command below with `<alias>` replaced by the key alias you want to use:
A keystore can store multiple keys, an alias is a way to specify which key.
In this case we only care about one key.
You can add a key to a keystore by running the command below
with an already existing `-keystore`

```
keytool -genkeypair -alias "<alias>" -keyalg RSA -keysize 4096 -validity 10000 -keypass:file pass -storepass:file pass -keystore release.keystore -v
```

You will be prompted for some more information,
provide what you wish to provide (press enter for default).

If everything went well, you should have a `release.keystore` file,
unfortunately, GitHub doesn't allow to upload files as secrets.
The easiest way to circumvent this for small files (such as the keystore),
is to base64-encode it.

On Linux you can do:

```bash
openssl base64 -A -in release.keystore -out release.keystore.base64
```

While on Windows you can do:

```powershell
[convert]::ToBase64String((Get-Content -path ".\release.keystore" -Encoding byte)) | Out-File -FilePath ".\release.keystore.base64"
```

Now we can move onto the repository
[Secrets](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions)
and [Variables](https://docs.github.com/en/actions/learn-github-actions/variables).

You can access them by:

1. Go to repository settings
2. Scroll down under Security
3. Secrets and variables
    - Actions
4. Repository secrets (if you're curious about the difference between environment secrets and
   repository secrets, you can look at [this SO answer](https://stackoverflow.com/a/65958690))

You will need to provide:

#### Secret: KEY_FILE_NAME

This is pretty much overkill and just to avoid naming conflicts, you can simply use
an [UUID](https://www.uuidgenerator.net/) (but just for this!)

#### Secret: KEY_STORE

This will need to be the contents of the `release.keystore.base64` file, without **any** line
breaks, including at the end. It will be decoded during the workflow.

#### Secret: KEY_STORE_ALIAS

This is the `<alias>` you provided during keystore creation. It identifies the key we want to use.

#### Secrets: KEY_PASSWORD and KEY_STORE_PASSWORD

Possibly the same or different depending on your means of generating the keystore.
They represent the `-keypass` anb `-storepass` parameters (contents of `pass` file).

#### Variable: DO_PUBLISH_REPO

During the workflow, the release repository is uploaded using
[artifact-upload](https://github.com/actions/upload-artifact)
(only members can access it).
So if you would like to provide some other means for users to access your
repository, you can stop here.

If however you would like to use your github repository as means to access the repo,
then you will need to set DO_PUBLISH_REPO to `true` (any other string will fail).

#### !IMPORTANT! -> Repo Branch

**This only needs to be done if DO_PUBLISH_REPO is `true`**

If you would like to use your github repository as a repo, you will need to create a `repo`
branch, possibly an empty one to avoid bringing your master branch git history.

This can be done quite easily, with a couple of notes:

- You should commit all changes you don't want to lose as this command will bring you into the
  context of the new branch, i.e. **nothing**. You don't _need_ to push them, but I would recommend
  doing so.

```bash
git switch --orphan repo
git commit --allow-empty -m "Repo branch"
git push -u origin repo
```

All files in this branch will be **cleared out** every release.
Use `git checkout master` to switch back to your master branch.
See [this SO answer](https://stackoverflow.com/a/34100189) for some more info.

Everything should be ready: go on GitHub on your repo, into Actions -> `Construct Release`
and click on the Run workflow button (run from the master branch).

At the end of the workflow run you should have 2 primary things:

1. `release-repo` artifact inside of the run (will expire in 1 day)
2. A commit by `github-actions[bot]` in the `repo` branch with message `Update repo`
   (if DO_PUBLISH_REPO === true)

Now you can direct your users to

```
https://raw.githubusercontent.com/{you}/{your-repo}/repo/index.min.json
```

The workflow will automatically attempt to purge
[jsdelivr](https://www.jsdelivr.com/)'s caches.
If for some reason your workflow fails at this step, the `repo` branch
should already be updated to the latest version.

-----

### Step 9

[Back to fast](#fast-setup)

By default the workflow only runs when requested to avoid unnecessary runs.
However, it can be configured to also run on push events quite easily, even though it's not
recommended, as it could cause inconsistencies between the installations of users
as tachiyomi doesn't prompt for an extension update unless the version code gets bumped.

To change it you can remove the commend at the beginning of the
[build_release](./.github/workflows/build_release.main.kts) file

```
on = listOf(WorkflowDispatch()/*, Push(branches = listOf("master"), pathsIgnore = listOf("**.md"))*/),
```

Pushes that only contain updates to markdown files will get ignored (see `pathsIgnore`).

Note that if you don't execute the kotlin script then the yaml file will not be updated
and the workflow will fail at the consistency check step.

To execute the script you need to use the
[kotlin cli compiler](https://kotlinlang.org/docs/command-line.html#install-the-compiler).
The script will execute relative to the script file, not the working directory, so you can call the
script from the `<root>` too.
