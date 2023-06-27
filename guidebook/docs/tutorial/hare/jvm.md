# JVM apps

{!tutorial/hare/start.md!}

## Create a sample project

Choose your path:

=== ":simple-kotlin: Jetpack Compose"

    * [x] Run the following commands.
    
    ```shell
    conveyor generate compose com.example.my-project
    cd my-project
    ./gradlew jar
    ```
    
=== ":fontawesome-brands-java: JavaFX"

    * [x] Run the following commands.
    
    ```shell
    conveyor generate javafx com.example.my-project
    cd my-project
    ./gradlew jar
    ```

{!tutorial/hare/github-tip.md!}


## Create the unpackaged app

{!tutorial/hare/create-unpackaged-app.md!}

## Serve the download site

{!tutorial/hare/serve-with-npx.md!}

{!tutorial/hare/serve-without-npx.md!}

## Release an update

In another terminal tab:

* [x] Edit `build.gradle[.kts]` and change the version field to `2.0`.
* [x] Run `./gradlew jar` to rebuild the app.
* [x] Run `conveyor make site` to regenerate the download site.

{!tutorial/hare/apply-update-instructions.md!}

## Read the config

It looks like this:

=== ":simple-kotlin: Jetpack Compose"

    ```
    include "#!./gradlew -q printConveyorConfig"
    
    app {
      site.base-url = "localhost:3000"
      display-name = "My Project"
      rdns-name = "com.example.my-project"
    
      icons = "icons/icon.svg"
    }
    ```

=== ":fontawesome-brands-java: JavaFX"

    ```
    include "#!./gradlew -q printConveyorConfig"
    
    app {
      site.base-url = "localhost:8899"
      display-name = "My Project"
      rdns-name = "com.example.my-project"
    
      icons {
        label = "FX"
      }
    
      // Ensure the icons are also included as data files. 
      // See the method HelloApplication.loadIconsFromStage().
      windows.inputs += TASK/rendered-icons/windows
      linux.inputs += TASK/rendered-icons/linux
    }
    ```

The include statement runs Gradle and uses its output as config. The [Conveyor Gradle plugin](../../configs/maven-gradle.md) adds tasks that
convert build config into Conveyor config.

{!tutorial/hare/hocon-tip.md!}

## Icons

=== ":simple-kotlin: Jetpack Compose"

    * [x] Delete the line that says `icons = icons/icon.svg`.
    * [x] Run `conveyor make rendered-icons`
    * [x] Look in the `output` directory to find your new icons.
    
    Basic, but functional.
    
    * [x] Add this code where the `icons` key was:
      ```
      icons {
         label = XY
         gradient = blue
      }
      ```
    * [x] Rerun `conveyor make rendered-icons` and look at the results again.

=== ":fontawesome-brands-java: JavaFX"

    * [x] Run `conveyor make rendered-icons`
    * [x] Look in the `output` directory to find your new icons.
    * [x] Marvel at their beauty and elegance.

    OK. More like basic but functional.

    * [x] Add `gradient = blue` to the `icons {}` object.
    * [x] Rerun `conveyor make rendered-icons` and look at the results again.

    The last two lines of the config use the output of the `rendered-icons` task as data files, so the results aren't just converted to
    platform native formats but also included as raw images. This lets you use them as stage icons.

{!tutorial/hare/learn-more-about-icons.md!}

## Upload a real update site

{!tutorial/hare/upload-real-site.md!}

## Signing

{!tutorial/hare/signing.md!}

## Become a 🐢 tortoise

{!tutorial/hare/become-a-tortoise.md!}

<script>var tutorialSection = 200;</script>
