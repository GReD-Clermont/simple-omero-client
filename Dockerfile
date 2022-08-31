FROM maven:3.8.6-jdk-8
MAINTAINER pierre.pouchin@uca.fr

COPY . /src
WORKDIR /src

CMD mvn package
