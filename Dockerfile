FROM maven:3.6.3-jdk-8
MAINTAINER pierre.pouchin@uca.fr

COPY . /src
WORKDIR /src

CMD mvn test
