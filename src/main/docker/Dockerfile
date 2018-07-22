FROM ${docker.image.from}
EXPOSE 8087
VOLUME /tmp
ADD maven/target/${project.artifactId}-exec.jar app.jar
ADD wait-for /wait-for
RUN sh -c 'touch /app.jar' && chmod a+x /wait-for
ENV JAVA_OPTS=""
ENV JAVA_RUN="-Djava.security.egd=file:/dev/./urandom -jar /app.jar"
ENV WAITFOR=""
ENTRYPOINT [ "sh", "-c", "if [ \"$WAITFOR\" != \"\" ]; then /wait-for $WAITFOR -- java $JAVA_OPTS $JAVA_RUN; else java $JAVA_OPTS $JAVA_RUN; fi" ]
