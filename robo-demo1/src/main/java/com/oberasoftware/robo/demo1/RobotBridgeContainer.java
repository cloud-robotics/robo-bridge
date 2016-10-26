package com.oberasoftware.robo.demo1;

import com.google.common.util.concurrent.Uninterruptibles;
import com.oberasoftware.robo.api.Robot;
import com.oberasoftware.robo.cloud.RemoteCloudDriver;
import com.oberasoftware.robo.cloud.RemoteConfiguration;
import com.oberasoftware.robo.cloud.motion.RemoteMotionEngine;
import com.oberasoftware.robo.core.CoreConfiguration;
import com.oberasoftware.robo.core.SpringAwareRobotBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

/**
 * @author Renze de Vries
 */
@SpringBootApplication
@Import({
        RemoteConfiguration.class,
        CoreConfiguration.class
})
public class RobotBridgeContainer {
    private static final Logger LOG = LoggerFactory.getLogger(RobotBridgeContainer.class);

    public static void main(String[] args) {
        LOG.info("Starting robot bridge");

        ApplicationContext context = new SpringApplication(RobotBridgeContainer.class).run(args);

        Robot max = new SpringAwareRobotBuilder("max", context)
                .motionEngine(RemoteMotionEngine.class)
                .remote(RemoteCloudDriver.class, true)
                .build();

        max.getMotionEngine().runMotion("Anim1");

        Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);
        max.getMotionEngine().stopAllTasks();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Killing the robot gracefully on shutdown");
            max.shutdown();
        }));
    }

}
