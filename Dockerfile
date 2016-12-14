FROM debian:stretch-slim

MAINTAINER Emmanuel Keller

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
          openjdk-8-jre-headless ca-certificates-java ghostscript tesseract-ocr tesseract-ocr-* \
 && rm -rf /var/lib/apt/lists/*

ADD target/qwazr-extractor-1.1-SNAPSHOT-exec.jar /usr/share/qwazr/qwazr-extractor.jar

VOLUME /var/lib/qwazr

EXPOSE 9091

WORKDIR /var/lib/qwazr/

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "/usr/share/qwazr/qwazr-extractor.jar"]
