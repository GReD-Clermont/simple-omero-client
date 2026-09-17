FROM maven:3-eclipse-temurin-11
LABEL org.opencontainers.image.authors="pierre.pouchin@uca.fr"

COPY . /src
WORKDIR /src

CMD ["mvn", "package"]
