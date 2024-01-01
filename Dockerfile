FROM eclipse-temurin:11.0.17_8-jre-ubi9-minimal
COPY ./dist-system-app/build/libs/dist-system-app.jar /usr/
EXPOSE 9999
EXPOSE 9998
EXPOSE 9997
EXPOSE 9996
CMD ["java", "-jar", "/usr/dist-system-app.jar"]