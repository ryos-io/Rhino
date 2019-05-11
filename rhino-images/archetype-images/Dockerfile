FROM adoptopenjdk/openjdk11:jdk-11.28-alpine as staging_area

RUN jlink \
    --module-path $JAVA_HOME/jmods \
    --verbose \
    --add-modules java.base,java.logging,java.xml,jdk.unsupported,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.scripting \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --strip-debug \
    --output /target/opt/jdk-minimal

RUN mkdir -p /target/usr/glibc-compat/lib; \
    cp /usr/glibc-compat/lib/libz* /target/usr/glibc-compat/lib

FROM frolvlad/alpine-glibc:alpine-3.8_glibc-2.28

COPY --from=staging_area /target /

RUN /usr/glibc-compat/sbin/ldconfig
RUN mkdir -p /opt/rhino/results

VOLUME [ "/opt/rhino/results" ]

ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    JAVA_HOME=/opt/jdk-minimal \
    PATH="$PATH:/opt/jdk-minimal/bin"
