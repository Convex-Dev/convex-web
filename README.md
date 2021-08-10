![Tests](https://github.com/Convex-Dev/convex-web/workflows/Tests/badge.svg)

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
