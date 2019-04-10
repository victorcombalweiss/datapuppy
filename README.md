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

## Known issues

If running inside a Docker container and reading from a log file on host machine, container's
clock has to be in sync with host machine, otherwise access log file might be read again in
whole every second, or conversely never read.
