<p align="center">
    <img src="https://github.com/victorcombalweiss/datapuppy/raw/master/src/main/resources/com/github/victorcombalweiss/datapuppy/logo-blue.jpg" width=300 align=middle/>
</p>

# Datapuppy

## Running

    ./gradlew dockerRemoveContainer dockerRun

or to pass arguments by command line:

    ./gradlew docker
    docker run -p 8080:8080 -p 9000:9000 datapuppy --trafficThreshold=15 --logFile=/tmp/other.log

### Command line options

- `-l` / `--logFile`: file path to read logs from. Default `/tmp/access.log`
- `-t` / `--trafficThreshold`: number of requests per second above which to declare an alert. Default `10`

## Watching

Point your web browser to `<docker-machine ip>:9000`.

Example: `http://192.168.99.100:9000`

## Generating logs

When container boots it starts up a test server that writes requests to the watched log file.

Send any request to `<docker-machine ip>:8080` to add a line to that log file.

Example: `http://192.168.99.100:8080/banana`


## Running automated tests

    ./gradlew test

## Roadmap

The next steps to push this project further would be:

### 1. Enable agent and server to run on separate machines

As of now, the communication channel between agent and server is the underlying filesystem.
To move forward it would probably be necessary to change that communication channel so that
they can easily be run on separate machines, which would make a lot more sense.

This would imply to change the build script in order to generate two different distribution
packages.

### 2. Start test server only when a given option is passed

As of now, starting the program always spins up a test server to populate the log file.
This should not happen in production, and thus there should be a command line option
to turn this on, with it being off by default.

### 3. Add integration tests

As of now, automated tests are all unit tests. It would be good to have a small set of
integration tests to prevent regressions from happening outside of unit tested logic.

### 4. Make front-end better structured

 - Move to Typescript
 - Move to Scss
 - Retrieve dependencies at build time and minify everything
 - Use handlebars-helpers instead of using custom helpers

### 5. Improve stats

 - Add a traffic graph
 - Add timestamp to stats so that web interface can give a sense of the freshness of the data
   to user

### 6. Add authentication to web app

As of now, anybody can access the web interface. This is a security flaw and ideally we
should protect data behind an authentication mechanism.

### 7. Add Javadoc

### 8. Support more access log formats

As of now, agent only supports 2 access log formats. It would be good to make it more generic
and support other most common log formats.

### 9. Enable to send a notification when alert is triggered

As of now, the only way to get alerted when traffic passes threshold is to watch the web
interface or the console output. It would make the program more useful if it could send
a notification by SMS, email or Slack to a given recipient upon alert trigger.

## Known issues

If running inside a Docker container and reading from a log file on host machine, container's
clock has to be in sync with host machine, otherwise access log file might be read again in
whole every second, or conversely never read.

## Acknowledgement

The Datapuppy logo is the work of my partner. All credit and thanks go to her!
