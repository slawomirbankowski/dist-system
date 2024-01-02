FROM eclipse-temurin:21.0.1_12-jre-ubi9-minimal
COPY ./dist-system-app/build/libs/dist-system-app.jar /usr/
EXPOSE 9996 9997 9998 9999
CMD ["java", "-jar", "/usr/dist-system-app.jar"]