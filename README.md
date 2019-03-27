## Running

    ./gradlew dockerRemoveContainer dockerRun

or to pass arguments by command line:

    ./gradlew docker
    docker run datapuppy myArg

## Generating logs

When container boots it starts up a test server that writes requests to the watched log file.

Send any request to `<docker-machine ip>:8080` to add a line to that log file.

Example: `http://192.168.99.100:8080/banana`
