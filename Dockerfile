FROM maven:3.6.3-jdk-11
MAINTAINER pierre.pouchin@uca.fr

COPY . /src
WORKDIR /src

CMD mvn package
