package competition.aspects;

import edu.wpi.first.wpilibj.RobotController;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.littletonrobotics.junction.Logger;
import xbot.common.command.BaseSubsystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Aspect
public class SubsystemTracer {

    private final Map<BaseSubsystem, Long> refreshDataFrameTimes = new HashMap<>();
    private final Map<BaseSubsystem, Long> periodicTimes = new HashMap<>();
    private final Set<BaseSubsystem> refreshDataFrameCalledMultipleTimes = new HashSet<>();
    private final Set<BaseSubsystem> periodicCalledMultipleTimes = new HashSet<>();

    @Before("execution(* edu.wpi.first.wpilibj.IterativeRobotBase.loopFunc(..))")
    public void clearMeasurements(JoinPoint joinPoint) {
        refreshDataFrameTimes.clear();
        periodicTimes.clear();
        refreshDataFrameCalledMultipleTimes.clear();
        periodicCalledMultipleTimes.clear();
    }

    @After("execution(* edu.wpi.first.wpilibj.IterativeRobotBase.loopFunc(..))")
    public void reportMeasurements(JoinPoint joinPoint) {
        // both should be the same length
        if (refreshDataFrameTimes.isEmpty()) {
            return;
        }

        Logger.recordOutput("Performance/Subsystems/Total/refreshDataFrame",
                refreshDataFrameTimes.values().stream().reduce(0L, Long::sum));
        Logger.recordOutput("Performance/Subsystems/Total/periodic",
                periodicTimes.values().stream().reduce(0L, Long::sum));
        Logger.recordOutput("Performance/Subsystems/Total/total",
                refreshDataFrameTimes.values().stream().reduce(0L, Long::sum)
                + periodicTimes.values().stream().reduce(0L, Long::sum));

        // get the longest refreshDataFrame time and its subsystem
        var maxRefreshDataFrame = refreshDataFrameTimes.entrySet().stream()
                .max(Map.Entry.comparingByValue()).get();
        Logger.recordOutput("Performance/Subsystems/Max/refreshDataFrame",
                maxRefreshDataFrame.getValue());
        Logger.recordOutput("Performance/Subsystems/Max/refreshDataFrameSubsystem",
                maxRefreshDataFrame.getKey().getName() + maxRefreshDataFrame.getKey().hashCode());

        // get the longest periodic time and its subsystem
        var maxPeriodic = periodicTimes.entrySet().stream()
                .max(Map.Entry.comparingByValue()).get();
        Logger.recordOutput("Performance/Subsystems/Max/periodic",
                maxPeriodic.getValue());
        Logger.recordOutput("Performance/Subsystems/Max/periodicSubsystem",
                maxPeriodic.getKey().getName() + maxPeriodic.getKey().hashCode());

        // get the subsystems that have been called multiple times
        Logger.recordOutput("Performance/Subsystems/MultipleCalls/refreshDataFrame",
                refreshDataFrameCalledMultipleTimes.stream().map(BaseSubsystem::getName).toArray(String[]::new));
        Logger.recordOutput("Performance/Subsystems/MultipleCalls/periodic",
                periodicCalledMultipleTimes.stream().map(BaseSubsystem::getName).toArray(String[]::new));
    }

    @Around("execution(* xbot.common.command.BaseSubsystem+.refreshDataFrame())")
    public void measureSubsystemRefreshDataFrame(ProceedingJoinPoint joinPoint) throws Throwable {
        var startTime = RobotController.getFPGATime();
        joinPoint.proceed();
        var endTime = RobotController.getFPGATime();

        Logger.runEveryN(5, () -> {
            var subsystem = (BaseSubsystem)joinPoint.getThis();
            if (refreshDataFrameTimes.containsKey(subsystem)) {
                // This could be a base class method call, ignore it
                refreshDataFrameCalledMultipleTimes.add(subsystem);
            } else {
                var time = endTime - startTime;
                refreshDataFrameTimes.put(subsystem, time);
                Logger.recordOutput("Performance/Subsystems/"
                        + subsystem.getName() + subsystem.hashCode()
                        + "/refreshDataFrame", refreshDataFrameTimes.get(subsystem));
            }
        });
    }

    @Around("execution(* xbot.common.command.BaseSubsystem+.periodic())")
    public void measureSubsystemPeriodic(ProceedingJoinPoint joinPoint) throws Throwable {
        var startTime = RobotController.getFPGATime();
        joinPoint.proceed();
        var endTime = RobotController.getFPGATime();

        Logger.runEveryN(5, () -> {
            var subsystem = (BaseSubsystem) joinPoint.getThis();
            if (periodicTimes.containsKey(subsystem)) {
                // This could be a base class method call, ignore it.
                periodicCalledMultipleTimes.add(subsystem);
            } else {
                var time = endTime - startTime;
                periodicTimes.put(subsystem, time);
                Logger.recordOutput("Performance/Subsystems/"
                        + subsystem.getName() + subsystem.hashCode()
                        + "/periodic", time);
            }
        });
    }
}
