FROM maven:3.8.6-jdk-8
LABEL org.opencontainers.image.authors="pierre.pouchin@uca.fr"

COPY . /src
WORKDIR /src

CMD ["mvn", "package"]
