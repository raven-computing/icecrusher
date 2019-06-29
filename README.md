# Icecrusher
#### An editor and analysis tool for DataFrame files

[![Release](https://img.shields.io/badge/release-2.1.0-blue.svg)](https://raven-computing.com/products/icecrusher) [![DownloadExe](https://img.shields.io/badge/Download_for_Windows-.exe-blue.svg)](https://raven-computing.com/products/icecrusher/releases/icecrusher-2.1.0.exe) [![DownloadDeb](https://img.shields.io/badge/Download_for_Linux-.deb-orange.svg)](https://raven-computing.com/products/icecrusher/releases/icecrusher-2.1.0.deb)

Icecrusher is an editor and analysis tool for DataFrame files (.df) with a modern-looking user interface. It is an open-source application and entirely implemented in JavaFX. It can read, edit, filter and create *.df* files. Additionally, it also suppports the capability to import and export CSV files. More information can be found on our [website](https://raven-computing.com/products/icecrusher/).

If you want to work with DataFrame files in your Java code, you can use the [Claymore](https://github.com/raven-computing/claymore/) library which is also open source.

You may download the [Demo](https://github.com/raven-computing/icecrusher/raw/master/demo/demo.df) file and try out Icecrusher for yourself.

[![Screenshot1](demo/screenshot1.png)](https://raw.githubusercontent.com/raven-computing/icecrusher/master/demo/screenshot1.png)

[![Screenshot2](demo/screenshot2.png)](https://raw.githubusercontent.com/raven-computing/icecrusher/master/demo/screenshot2.png)

## Installation

There are installers available for both **Windows** and **Linux**. When executed, they will install Icecrusher on your operating system. 
You don't necessarily need a JRE and JavaFX as the installer provides a private copy of a native JRE which will be used by the application. 

## Build

If you want to build Icecrusher from source you will need the JDK and JavaFX (both minimum version 8).
We are currently using the *javafx-maven-plugin* to build the packages, although this may change in the future.
The standard *mvn package* command will package the application as a *jar* and put it into the *build/dist/app/* directory. That will not, however, build a self-contained application bundle that includes a JRE. As long as you have a JRE and JavaFX installed on your system you can directly execute *icecrusher.jar*. If you want to build a native installer, we suggest you use either *dpgk-deb* (on linux) or *Inno Setup* (on windows), depending on your operating system.

## Contact

If you encountered a bug or have a feature request, consider opening an issue on GitHub.
Alternatively, you may send us an e-mail: info@raven-computing.com

## License

Icecrusher is licensed under the Apache License Version 2 - see the [LICENSE](LICENSE) for details.
The JRE which is part of each native installer is subject to its own license. Please inform yourself about such if you plan to build, extend, distribute either this application or any products as part of the openJDK.
Disclaimer: Raven Computing does not own, maintain or sell any software product as part of the openJDK or JRE.

