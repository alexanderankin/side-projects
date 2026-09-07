FROM ubuntu:26.04
RUN DEBIAN_FRONTEND=noninteractive apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y build-essential liblzma-dev gcc-x86-64-linux-gnu
#ARG CROSS_COMPILER_NAME
#RUN DEBIAN_FRONTEND=noninteractive apt-get install -y gcc-${CROSS_COMPILER_NAME}-linux-gnu
