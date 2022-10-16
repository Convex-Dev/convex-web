![Tests](https://github.com/Convex-Dev/convex-web/workflows/Tests/badge.svg)

## Configuration

### Secrets

The default configuration - config.edn - requires a `~/.convex/secrets.edn` file.

You can copy the example `secrets.example.edn` and configure the passphrase.

## Development

### Prerequisites
- [Java Development Kit 11](https://adoptopenjdk.net/)
- [Clojure CLI](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools)

### App

Install dependencies:

```
npm install
```

Compile CSS:
```
npm run styles:watch
```

Run Shadow CLJS:
```
npm run app:watch
```

### Server

Run REPL with the `dev` task, providing any number of required aliases:

    bb dev :module/server

    bb dev '[:module/app :module/server]'

Start server on port 8080:
```
(go)
```

## Docker Testing

To run convex-web as a local docker container, you will need to do the following.

Build the docker image
```
docker build -t convex-web .
```

Run the docker image
```
docker run --publish 8080:8080 convex-web
```

## Production

### App

Install dependencies:

```
npm install
```

Compile & bundle app:
```
npm run app:release
```

Compile CSS:
```
npm run styles:release
```

### Server

```
bin/run
```

### Create a Linux `systemd` Service

- Copy:
    `deployment/convex_web.service` to `/etc/systemd/system/convex_web.service`;
- Enable:
    `sudo systemctl enable convex_web`
- Start:
    `sudo systemctlm start convex_web`
