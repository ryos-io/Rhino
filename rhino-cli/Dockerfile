#
# ADOBE CONFIDENTIAL
# ___________________
#
#  Copyright 2018 Adobe Systems Incorporated
#  All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains
# the property of Adobe Systems Incorporated and its suppliers,
# if any.  The intellectual and technical concepts contained
# herein are proprietary to Adobe Systems Incorporated and its
# suppliers and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Adobe Systems Incorporated.

#
# rhino: loadtesting image.
# -
# version   1.0
# author    Erhan Bagdemir <bagdemir@adobe.com>

FROM alpine:3.6
MAINTAINER Erhan Bagdemir <bagdemir@adobe.com>

# Install required packages.
RUN apk -v --update add \
    	openssh-client \
    	bash \
        curl \
        openjdk8 \
        jq \
        git \
        python \
        py-pip \
        groff \
        less \
        mailcap \
        && \
    pip install --upgrade awscli==1.14.5 s3cmd==2.0.1 python-magic && \
    apk -v --purge del py-pip && \
    rm /var/cache/apk/*

COPY sc-dev-eu.pem /root/.ssh/sc-dev-eu.pem

# Install gatling.
# working directory for gatling
WORKDIR /opt

# Gating version
ENV GATLING_VERSION 2.2.5

# create directory for gatling install
RUN mkdir -p gatling

# install gatling
RUN mkdir -p /tmp/downloads && \
  curl -sf -o /tmp/downloads/gatling-$GATLING_VERSION.zip \
  -L https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/$GATLING_VERSION/gatling-charts-highcharts-bundle-$GATLING_VERSION-bundle.zip && \
  mkdir -p /tmp/archive && cd /tmp/archive && \
  unzip /tmp/downloads/gatling-$GATLING_VERSION.zip && \
  mv /tmp/archive/gatling-charts-highcharts-bundle-$GATLING_VERSION/* /opt/gatling/

# change context to gatling directory
WORKDIR  /opt/gatling

# set directories below to be mountable from host
ENV GATLING_HOME /opt/gatling/
ENV PATH "$PATH:/opt/gatling/bin:/usr/local/rhino"

VOLUME ["/opt/gatling/results", "/usr/local/rhino"]

ENTRYPOINT [ "/usr/local/rhino/rhino-exec" ]
