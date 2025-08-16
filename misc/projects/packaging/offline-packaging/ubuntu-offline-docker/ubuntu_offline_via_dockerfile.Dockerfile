FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

ARG GID
USER root:$GID

ARG host_area
ARG host_zone
RUN \
    umask 0002 && \
    rm -v /etc/apt/apt.conf.d/01autoremove && \
    rm -v /etc/apt/apt.conf.d/docker-autoremove-suggests && \
    rm -v /etc/apt/apt.conf.d/docker-clean && \
    sed -i '/AlwaysOnline/d' /etc/apt/apt.conf.d/01-vendor-ubuntu && \
    echo "tzdata tzdata/Areas select $host_area" | debconf-set-selections && \
    echo "tzdata tzdata/Zones/America select $host_zone" | debconf-set-selections && \
    yes | unminimize

RUN apt update
RUN apt upgrade -y
RUN echo 'Binary::apt::APT::Keep-Downloaded-Packages "true";' | tee /etc/apt/apt.conf.d/99-keep-downloads
#RUN apt install jq -y
RUN apt install -y apt-utils software-properties-common tzdata
RUN apt install -y git python3 python3-venv python-is-python3 nodejs htop jq net-tools iproute2 zip unzip curl wget openssh-server bat vim vim-tiny
RUN apt install -y build-essential software-properties-common git curl wget vim zip unzip htop python3 python3-venv postgresql docker.io gnupg make cmake clang jq net-tools iproute2 nmap whois ethtool rustc strace bridge-utils iputils-ping sqlite3 redshift mold imagemagick-6.q16 gparted bat ripgrep tree pv watch openssh-server nmap alacritty
RUN apt install -y librust-starship-module-config-derive-dev
RUN apt install -y nginx
RUN apt install -y i3 i3-wm i3blocks i3lock-fancy i3status dmenu picom unclutter-xfixes feh xdotool psmisc
RUN apt install -y xclip
