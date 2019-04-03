## Running

    ./gradlew dockerRemoveContainer dockerRun

or to pass arguments by command line:

    ./gradlew docker
    docker run -p 8080:8080 -p 9000:9000 datapuppy --trafficThreshold=15

## Watching

Point your web browser to `<docker-machine ip>:9000`.

Example: `http://192.168.99.100:9000`

## Generating logs

When container boots it starts up a test server that writes requests to the watched log file.

Send any request to `<docker-machine ip>:8080` to add a line to that log file.

Example: `http://192.168.99.100:8080/banana`


## Running automated tests

    ./gradlew test
