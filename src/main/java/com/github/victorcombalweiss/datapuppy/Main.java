package com.github.victorcombalweiss.datapuppy;

import com.github.victorcombalweiss.datapuppy.agent.Agent;
import com.github.victorcombalweiss.datapuppy.testserver.TestServer;

public class Main {

    public static void main(String[] args) throws Exception {
        final String accessLogFilePath = "/tmp/access.log";

        TestServer.main(new String[] { accessLogFilePath });
        Agent.main(new String[] { accessLogFilePath });
    }
}
