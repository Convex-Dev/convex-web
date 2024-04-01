# Docker for convex web

FROM node:latest

# FROM openjdk:23-bookworm

ENV HOME=/home/convex-web

RUN apt-get update
RUN apt-cache search jdk
RUN apt-get install -y curl openjdk-17-jdk


# install node 18.x
# RUN curl -sL https://deb.nodesource.com/setup_18.x | bash
# RUN apt-get install -y nodejs
# RUN curl -L https://www.npmjs.com/install.sh | sh


# install latest clojure
RUN curl -L -O https://github.com/clojure/brew-install/releases/latest/download/posix-install.sh
RUN chmod +x posix-install.sh
RUN ./posix-install.sh


WORKDIR $HOME

ADD . $HOME

RUN npm install
RUN npm run app:release
RUN npm run styles:release

# setup key file storage and access details
RUN rm -rf $HOME/.convex
RUN mkdir -p $HOME/.convex
RUN echo "{:key-store-passphrase \"password\" \
 :key-passphrase \"password\"}" > $HOME/.convex/secrets.edn
ADD config.edn $HOME/.convex/config.edn
RUN rm $HOME/config.edn
RUN ln -s $HOME/.convex/config.edn $HOME/config.edn

CMD clojure \
  -J--add-opens=java.base/java.nio=ALL-UNNAMED \
  -J--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED \
  -J--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  -J-Duser.home=$HOME \
  -M:main:repl:logback-dev \
  -e "(go)"
