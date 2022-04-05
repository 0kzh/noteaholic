<br />
<div align="center">
  <a href="https://git.uwaterloo.ca/lfattakh/cs398project">
    <img src="client/src/main/resources/img/noteaholic.svg" alt="Logo" width="400" height="100">
  </a>

  <h3 align="center">A Canvas Based Note Taking Application</h3>
  <h4 align="center">Leon Fattakhov, Advait Maybhate, Steven Xu, Kelvin Zhang</h3>
</div>


## Table of Contents
[[_TOC_]]

## About the Project

Noteaholic is a free-form, spatial note-taking desktop application. It supports cloud-based storage of notes, with a custom user accounts system. We aim to emulate the organization of notes on a physical desk, with our unique approach of being able to move sticky notes on an infinite canvas.


## Quick Start Guide

<div align="center">
<img src="docs/AppScreenShot.png" height="400">
</div>
<br />
<br />

Currently the application is built for `macOS` to install the application first download the latest distribution of our application [here](https://git.uwaterloo.ca/lfattakh/cs398project/-/raw/master/client/client-1.0.0.dmg?inline=false). Please see our Wiki Home page if you run into "client.app is damaged" issues when opening the application - this is an error caused by Apple's security restrictions (our Wiki page outlines how to bypass it).

Additional information is included in our [wiki](https://git.uwaterloo.ca/lfattakh/cs398project/-/wikis/home)


## Contributing

This project requires the following tools to be installed to compile and run the code.
* Docker
* Docker Compose
* Kotlin

This project is broken into two separate portions:
* [The Server](https://git.uwaterloo.ca/lfattakh/cs398project/-/tree/master/server)
* [The Client](https://git.uwaterloo.ca/lfattakh/cs398project/-/tree/master/client)

Each of these have their own `README.md` files to help setup the development environment.
However the server should be launched before the client in order to speed up development. As the client will switch to an empty state waiting until it detects that the server is running.

Both projects have been developed using JetBrains IntelliJ IDEA and as a result have configurations for this IDE in the `.idea` folders. However you can use any code editor or tool that you wish to use. The build system used for both these projects is Gradle with the Kotlin DSL. As a result commonly used gradle tasks are available in both projects. Some common Gradle tasks used in development are:
* `gradlew build` - builds the project
* `gradlew test` - runs the unit tests for the project
* `gradlew run` - runs the project


## The Team

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img src="https://avatars.githubusercontent.com/coolcom200" title="Leon Fattakhov" width="120" height="120"> 
        <p>Leon Fattakhov</p>
      </td>
      <td align="center">
        <img src="https://ca.slack-edge.com/T025KUF9Y-UGNHHES3B-240900ed5074-512" title="Advait Maybhate" width="120" height="120"> 
        <p>Advait Maybhate</p>
      </td>
      <td align="center">
        <img src="https://ca.slack-edge.com/T025KUF9Y-UUXC7TP9R-374ac6303780-72" title="Steven Xu" width="120" height="120"> 
        <p>Steven Xu</p>
      </td>
      <td align="center">
        <img src="https://ca.slack-edge.com/T025KUF9Y-U01T5FQ1J77-b91b6953a1c6-512" title="Kelvin Zhang" width="120" height="120"> 
        <p>Kelvin Zhang</p>
      </td>
    </tr>
  </table>
</div>

## License

This project uses the GNU General Public License v3.0. The full details can be found in our [`LICENSE`](./LICENSE) file.

## Third Party Libraries & Code

### Citations
- Borders.kt - https://gist.github.com/gildor/ff7f56da7216ae9e4da77368a4beb87a
- K9 Email Validation Regex - https://github.com/k9mail/k-9
- Kotlin resources retrieval - https://stackoverflow.com/questions/42739807/how-to-read-a-text-file-from-resources-in-kotlin
- Canvas focusing in Jetpack Compose - https://medium.com/google-developer-experts/focus-in-jetpack-compose-6584252257fe
- Order of focus modifiers - https://stackoverflow.com/questions/70015530/unable-to-focus-anything-other-than-textfield
- How to create a fat JAR with dependencies - https://stackoverflow.com/questions/41794914/how-to-create-a-fat-jar-with-gradle-kotlin-script
- Improving performance with keys - https://pankaj-rai.medium.com/jetpack-compose-optimize-list-performance-with-key-1066567339f9
- Parsing Bold HTML Tags in a clean way - https://stackoverflow.com/questions/66494838/android-compose-how-to-use-html-tags-in-a-text-view

### Libraries
  - JetBrains Exposed SQL Framework - https://github.com/JetBrains/Exposed
  - Ktor Server and Client - https://github.com/ktorio/ktor
  - Xerial SQLite - https://github.com/xerial/sqlite-jdbc
  - Postgres PGJDBC-NG - https://github.com/impossibl/pgjdbc-ng
  - Mindrot JBCrypt - https://www.mindrot.org/projects/jBCrypt/
  - JetBrains Compose Desktop - https://github.com/JetBrains/compose-jb
  - Androidx Jetpack Compose - https://github.com/androidx/androidx
  - SendGrid (for sending emails) - https://docs.sendgrid.com/for-developers/sending-email/v3-java-code-example
  - Compose Color Picker - https://github.com/godaddy/compose-color-picker
  - JetBrains Markdown Parser - https://github.com/JetBrains/markdown
  - Kotlinx Datetime - https://github.com/Kotlin/kotlinx-datetime/

