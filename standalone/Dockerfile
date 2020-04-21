FROM openjdk:8

RUN mkdir /app
RUN groupadd app && useradd -s /bin/false -d /app -g app app
RUN chown app:app /app
USER app

VOLUME /tmp
RUN mkdir -p /tmp/bti/lucene/index
EXPOSE 8080

WORKDIR /app

COPY ./target/standalone-*jar-with-dependencies.jar /app/app.jar

ENTRYPOINT java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n $JAVA_OPTS -jar /app/app.jar
