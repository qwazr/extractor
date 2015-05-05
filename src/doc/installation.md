Installation
============

Requirement
-----------

**oss-extractor** requires a [JAVA Runtime Environment 7 or newer](http://openjdk.java.net/install/)

Download the binary package
---------------------------

- The official releases are available here: [Stable Releases](https://github.com/qwazr/qwazr-extractor/releases)
- The nightly builds are available here: [Snapshot Releases](http://download.open-search-server.com/ftp/oss-extractor/?C=M;O=D)

The following files are available:

- The RPM package for CentOS, RedHat, Fedora: oss-extractor-x.x.x-1.noarch.rpm
- The DEB package for Debian, Ubuntu: oss-extractor-x.x.x-SNAPSHOT.deb
- Executable JAR file for a manual installation: oss-extractor-x.x.x-SNAPSHOT-exec.jar

Install using the package manager
---------------------------------

### RPM (CentOS, Fedora, RedHat)

This is a standard RPM package.

    rpm -ivh oss-extractor-x.x.x-1.noarch.rpm

### DEB (Debian, Ubuntu)

This is a standard DEB package.

    dpkg -i oss-extractor-x.x.x-SNAPSHOT.deb
    
### Running

To start the daemon:

    service oss-extractor start
    
To stop the daemon

    service oss-extractor stop
    
The status of the daemon:

    service oss-extractor status

### What is installed

- **dir** /var/lib/opensearchserver : The data directory
- **dir** /usr/share/opensearchserver/
- **file** /usr/share/opensearchserver/opensearchserver : A copy of the init.d scrip
- **file** /usr/share/opensearchserver/oss-extractor.jar : The binary
- **dir** /var/log/opensearchserver/oss-extractor : contains the log files
- **dir** /etc/opensearchserver : contains the configuration files
- **dir** /etc/opensearchserver/oss-extractor : the configuration file

### The configuration file

The configuration file is located in /etc/opensearchserver/oss-extractor.
You can change the TCP port used by the server. You may also change the default memory allocation.

```shell
# The TCP port used by the server
SERVER_PORT=9091


# Any JAVA option. Often used to allocate more memory usage.
#JAVA_OPTS="-Xms1G -Xmx4G"
```

Manual installation
-------------------

Just start the program:

    java -jar oss-extractor-x.x.x-SNAPSHOT-exec.jar
    
To change the TCP port, use the -p parameter:

    java -jar oss-extractor-x.x.x-SNAPSHOT-exec.jar


    
