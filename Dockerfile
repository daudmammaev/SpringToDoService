FROM ubuntu:latest
LABEL authors="user"
ARG JAR_FILE=out/artifacts/SpringToDo_jar/SpringToDo.jar
ENTRYPOINT ["top", "-b", "java","-jar","SpringToDo.jar"]


