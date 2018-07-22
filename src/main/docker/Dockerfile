FROM ${docker.image.from}
EXPOSE 8087
VOLUME /tmp

# Add JAR from project (required docker-maven-plugin at least 0.23.0)
ADD maven/target/${project.artifactId}-exec.jar app.jar
ADD wait-for /wait-for
RUN touch /app.jar && chmod 0644 /app.jar && chmod a+x /wait-for

# Runtime parameters (docker run ... org docker-compose environment: ...)
ENV JAVA_OPTS=""
ENV JAVA_RUN="-Djava.security.egd=file:/dev/./urandom -jar /app.jar"
ENV WAITFOR=""

# Rename java so it can be identified in processes
# Specify at build time (JAVA_BINARY=xxx docker build)
ARG JAVA_BINARY="/usr/bin/java"
RUN if [ "$JAVA_BINARY" != "/usr/bin/java" ]; then echo "Making link $JAVA_BINARY"; ln -s /usr/bin/java $JAVA_BINARY; fi

# Also specify at runtime
ENV JAVA_BINARY="/usr/bin/java"

# Create user
ARG USERNAME=java
ARG USERID=1000
ARG HOME=/tmp
RUN useradd -m -d $HOME -s /bin/bash $USERNAME
USER $USERNAME
ENV HOME=/tmp

# Run unprivileged, specify JAVA_BINARY, WAITFOR, JAVA_OPTS, JAVA_RUN or use defaults.
ENTRYPOINT [ "sh", "-c", "if [ \"$WAITFOR\" != \"\" ]; then /wait-for $WAITFOR -- $JAVA_BINARY $JAVA_OPTS $JAVA_RUN; else $JAVA_BINARY $JAVA_OPTS $JAVA_RUN; fi" ]
