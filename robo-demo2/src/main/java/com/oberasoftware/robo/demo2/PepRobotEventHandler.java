package com.oberasoftware.robo.demo2;

import com.oberasoftware.base.event.EventHandler;
import com.oberasoftware.base.event.EventSubscribe;
import com.oberasoftware.robo.api.Robot;
import com.oberasoftware.robo.api.events.ValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Renze de Vries
 */
public class PepRobotEventHandler implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PepRobotEventHandler.class);

    private Robot max;

    public PepRobotEventHandler(Robot max) {
        this.max = max;
    }

    @EventSubscribe
    public void receive(ValueEvent valueEvent) {
        LOG.info("Received an event for pep: {}", valueEvent);
//        if (valueEvent.getControllerId().equals("peppy") && valueEvent.getItemId().equals("head")) {
////            if (valueEvent.getValue().asString().equals("true")) {
//
////                max.getMotionEngine().runMotion("Bravo");
//            }
//        }
    }
}
