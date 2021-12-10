# Deployment

The program is launched with the Clojure CLI - see `bin/run` script - and it's installed as a system service.

There's a script to start, stop and restart the service in the `bin/` directory.

## Update convex.world

The most common procedure to update convex.world is: pull changes and restart the service.

You must run the scripts as `convex`.

### Step by step

1. Go to convex-web local Git repository in `/usr/local/convex/convex-web`.
2. Pull changes `git pull origin master` - switch to `develop` if you would like to deploy a version still in development.
3. Restart the service `bin/restart`
4. Check logs `log/all.log`

### Reseting the database

When a database reset is required, you must stop the service, and clear the database - once the application is stopped, delete `/home/convex/.convex/etch` and `/home/convex/.convex/datalevin`.

