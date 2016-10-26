package com.oberasoftware.robo.demo2;

import com.oberasoftware.base.event.EventHandler;
import com.oberasoftware.base.event.EventSubscribe;
import com.oberasoftware.robo.api.Robot;
import com.oberasoftware.robo.api.events.ValueEvent;
import com.oberasoftware.robo.api.motion.WalkDirection;
import com.oberasoftware.robo.api.motion.controller.HandsController;
import com.oberasoftware.robo.api.servo.ServoDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Renze de Vries
 */
public class MaxRobotEventHandler implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MaxRobotEventHandler.class);
    public static final int FORWARD = 1;
    public static final int STOPPED = 0;
    public static final int BACKWARD = -1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    private Robot pep;

    private AtomicBoolean handsOpen = new AtomicBoolean(true);

    private AtomicInteger walkMode = new AtomicInteger(0);

    public MaxRobotEventHandler(Robot pep) {
        this.pep = pep;
    }

    @EventSubscribe
    public void receive(ValueEvent valueEvent) {
        String controllerId = valueEvent.getControllerId();
        String itemId = valueEvent.getItemId();

        if (controllerId.equals("max") && itemId.equals("Hand")) {
            handleHandsEvent(valueEvent);
        } else if(controllerId.equals("max") && itemId.equals("HandYaw")) {
            handleHandsRoll(valueEvent);
        }
        if(handsOpen.get()) {
            if (controllerId.equals("max") && itemId.equals("Walk")) {
                handleWalk(valueEvent);
            } else if (controllerId.equals("max") && itemId.equals("WalkDirection")) {
                handleWalkDirection(valueEvent);
            }
        }
    }

    private void handleHandsEvent(ValueEvent valueEvent) {
        LOG.info("Detected hands movement from: {} for: {}/{} value: {}", valueEvent.getControllerId(), valueEvent.getItemId(), valueEvent.getLabel(), valueEvent.getValue());

        int position = valueEvent.getValue().getValue();
        Optional<HandsController> handsController = pep.getMotionEngine().getMotionController(HandsController.CONTROLLER_NAME);
        if (position < 500 && handsOpen.compareAndSet(true, false)) {
            LOG.info("Closing hands");
            handsController.get().closeHands();
        } else if(position > 500 && handsOpen.compareAndSet(false, true)) {
            LOG.info("Opening hands");
            handsController.get().openHands();
        }
    }

    private void handleHandsRoll(ValueEvent valueEvent) {
        LOG.info("Detected hands rolling movement from: {} for: {}/{} value: {}", valueEvent.getControllerId(), valueEvent.getItemId(), valueEvent.getLabel(), valueEvent.getValue());

        int position = valueEvent.getValue().getValue();
        ServoDriver servoDriver = pep.getServoDriver();
        servoDriver.setTargetPosition("RWristYaw", position);
    }

    private void handleWalkDirection(ValueEvent valueEvent) {
        int position = valueEvent.getValue().getValue();

        if(position > 600 && walkMode.compareAndSet(FORWARD, LEFT)) {
            //walk left
            pep.getMotionEngine().walk(WalkDirection.LEFT);
        } else if(position < 400 && walkMode.compareAndSet(FORWARD, RIGHT)) {
            //walk right
            pep.getMotionEngine().walk(WalkDirection.RIGHT);
        } else if(walkMode.get() > FORWARD) {
            //walk forward
            pep.getMotionEngine().walk(WalkDirection.FORWARD);
            walkMode.set(FORWARD);
        }
    }

    private void handleWalk(ValueEvent valueEvent) {
        int position = valueEvent.getValue().getValue();

        if(position >650 && position < 750 && walkMode.compareAndSet(STOPPED, FORWARD)) {
            LOG.info("Walking forward");
            pep.getMotionEngine().walk(WalkDirection.FORWARD);
        } else if(position >=750 && position <=820 && walkMode.get() != STOPPED) {
            LOG.info("Stop walking");
            pep.getMotionEngine().stopWalking();
            walkMode.set(0);
        } else if(position >820 && walkMode.compareAndSet(STOPPED, BACKWARD)) {
            LOG.info("Walking backward");
            pep.getMotionEngine().walk(WalkDirection.BACKWARD);
        }
    }
}
