# Docker for convex web

# base docker on latest node but install java 17
# currently this is the only working solution for arm64 builds.
# FROM node:latest

# we can base the docker image on java 23, but install node
FROM openjdk:23-bookworm

ENV HOME=/home/convex-web


RUN apt-get update

# install the standard java 17 from the node:latest build
# RUN apt-cache search jdk
# RUN apt-get install -y curl openjdk-17-jdk


# install node 18.x if we are using the java base docker image
RUN curl -sL https://deb.nodesource.com/setup_20.x | bash
RUN apt-get install -y nodejs
RUN curl -L https://www.npmjs.com/install.sh | sh

# for arm64 only
RUN apt-get install -y liblmdb-dev

# install latest clojure
RUN curl -L -O https://github.com/clojure/brew-install/releases/latest/download/posix-install.sh
RUN chmod +x posix-install.sh
RUN ./posix-install.sh


WORKDIR $HOME

ADD . $HOME

# remove old jar files
RUN rm -f $HOME/*.jar

RUN npm install
RUN npm run app:release
RUN npm run styles:release

# setup key file storage and access details
# for security this needs to be moved late to an external mount point
RUN rm -rf $HOME/.convex
RUN mkdir -p $HOME/.convex
RUN echo "{:key-store-passphrase \"password\" \
 :key-passphrase \"password\"}" > $HOME/.convex/secrets.edn
ADD config.edn $HOME/.convex/config.edn
RUN rm $HOME/config.edn
RUN ln -s $HOME/.convex/config.edn $HOME/config.edn


# startup the web and peer services
CMD clojure \
  -J--add-opens=java.base/java.nio=ALL-UNNAMED \
  -J--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED \
  -J--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  -J-Duser.home=$HOME \
  -M:main:repl:logback-dev \
  -e "(go)"
