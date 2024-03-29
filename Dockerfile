# Docker for convex web

FROM node:latest

ENV HOME=/home/convex-web

ENV CLOJURE_VER=1.10.3.933

RUN apt-get update \
  && apt-get -q -y install openjdk-11-jdk curl \
  && npm install -g shadow-cljs \
  && curl -s https://download.clojure.org/install/linux-install-$CLOJURE_VER.sh | bash

WORKDIR $HOME

ADD . $HOME

RUN npm install

RUN npm run app:release

RUN npm run styles:release

RUN rm -rf $HOME/.convex
RUN mkdir -p $HOME/.convex
RUN echo "{:key-store-passphrase \"password\" \
 :key-passphrase \"password\"}" > $HOME/.convex/secrets.edn
ADD config.edn $HOME/.convex/config.edn
RUN rm $HOME/config.edn
RUN ln -s $HOME/.convex/config.edn $HOME/config.edn

CMD clojure -J-Duser.home=$HOME -M:main:repl:logback-prod -e "(go)"
