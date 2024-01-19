FROM eclipse-temurin:8-jre-alpine

WORKDIR /nexus

RUN \
    apk add --update --no-cache curl && \
    curl -fSsLO http://download.sonatype.com/nexus/3/nexus-3.64.0-04-unix.tar.gz && \
    tar xzf nexus-3.64.0-04-unix.tar.gz && \
    rm nexus-3.64.0-04-unix.tar.gz && \
    echo pwd && pwd && echo ls -la && ls -la

CMD ["./nexus-3.64.0-04/bin/nexus", "run"]
EXPOSE "8081/tcp"

# verify this somehow?
VOLUME "/nexus/sonatype-work/nexus3"

# use "nexus" user with uid 200 for compatibility with official image
RUN \
    addgroup nexus && \
    adduser --home /nexus/sonatype-work --shell /bin/bash --no-create-home --uid 200 -G nexus --disabled-password nexus
USER nexus
