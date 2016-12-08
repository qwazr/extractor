FROM openjdk:8-jdk-alpine

MAINTAINER Emmanuel Keller

ADD target/qwazr-extractor-1.1-SNAPSHOT-exec.jar /usr/share/qwazr/qwazr-extractor.jar

VOLUME /var/lib/qwazr

EXPOSE 9091

WORKDIR /var/lib/qwazr/

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "/usr/share/qwazr/qwazr-extractor.jar"]
