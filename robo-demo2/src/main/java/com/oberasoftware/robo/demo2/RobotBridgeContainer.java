package com.oberasoftware.robo.demo2;

import com.oberasoftware.robo.api.Robot;
import com.oberasoftware.robo.cloud.RemoteCloudDriver;
import com.oberasoftware.robo.cloud.RemoteConfiguration;
import com.oberasoftware.robo.cloud.RemoteServoDriver;
import com.oberasoftware.robo.cloud.RemoteSpeechEngine;
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

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;

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
                .servoDriver(RemoteServoDriver.class)
                .remote(RemoteCloudDriver.class, true)
                .build();

        Robot pep = new SpringAwareRobotBuilder("peppy", context)
                .motionEngine(RemoteMotionEngine.class)
                .capability(RemoteSpeechEngine.class)
                .servoDriver(RemoteServoDriver.class)
                .remote(RemoteCloudDriver.class, true)
                .build();

        max.getMotionEngine().runMotion("ArmControl");
        sleepUninterruptibly(10, TimeUnit.SECONDS);
        max.getServoDriver().setTorgue("15", false);
        max.getServoDriver().setTorgue("5", false);
        max.getServoDriver().setTorgue("2", false);
        LOG.info("Robot ready for demo");

        MaxRobotEventHandler maxHandler = new MaxRobotEventHandler(pep);
        max.listen(maxHandler);

        PepRobotEventHandler pepHandler = new PepRobotEventHandler(max);
        pep.listen(pepHandler);

        pep.getMotionEngine().prepareWalk();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Killing the robot gracefully on shutdown");
            max.shutdown();
        }));
    }

}
