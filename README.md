![Screenshot](/doc/screenshot.png?raw=true)

## Development

### Prerequisites
- [Install Maven](https://maven.apache.org/download.cgi)
- Install Convex
  - Clone git@github.com:nuroko/convex.git
  - Switch to `convex` directory and run `mvn clean install -DskipTests`
- [Install Clojure CLI](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools)

### App

Install dependencies:

```
npm install
```

Run Shadow CLJS:
```
npm run watch
```

### Server

Run REPL with the `dev` alias:
```
clj -A:dev
```

Start server on port 8080:
```
(go)
```

## Production

### App

Install dependencies:

```
npm install
```

Compile & bundle app:
```
npx shadow-cljs release main
```

### Server

```
clj -A:main
```

### Create a Linux `systemd` Service

- Copy:
    `deployment/convex_web.service` to `/etc/systemd/system/convex_web.service`;
- Enable:
    `sudo systemctl enable convex_web`
- Start:
    `sudo systemctlm start convex_web`
    
## Logging

Google Cloud Logging is used for log storage, and there are two types of logs in Convex Web:
- SLF4J/Logback for Convex and libraries,
- and u/Log events for structured log.

u/Log Events:
- `:logging.event/endpoint`
- `:logging.event/system-error`;
- `:logging.event/user-error`;
- `:logging.event/repl-user`;
- `:logging.event/repl-error`;
- `:logging.event/faucet`;
- `:logging.event/new-account`;
- `:logging.event/confirm-account`;

There's also a Google Cloud Logging Sink to send events log to Big Query. Once our events log are in Big Query we can use SQL to query all different sort of things. (There are some saved queries in project)