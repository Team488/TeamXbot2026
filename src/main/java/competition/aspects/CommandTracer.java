package competition.aspects;

import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj2.command.Command;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.sensors.XTimer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// CHECKSTYLE:OFF
@Aspect
public class CommandTracer {

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(CommandTracer.class);

    private final Map<Command, Double> commandStartTimes = new HashMap<>();
    private final Map<Command, Alert> runningCommandAlerts = new HashMap<>();
    private final LinkedList<Alert> completedAlertList = new LinkedList<>();

    @Before("execution(* edu.wpi.first.wpilibj.IterativeRobotBase.loopFunc(..))")
    public void clearAlertsForFinishedCommands(JoinPoint joinPoint) {
        while (!completedAlertList.isEmpty()) {
            completedAlertList.poll().close();
        }
    }

    @Before("execution(* edu.wpi.first.wpilibj2.command.Command+.initialize(..))" +
            "|| execution(* edu.wpi.first.wpilibj2.command.Command+.execute(..))")
    public void createCommandAlert(JoinPoint joinPoint) {
        if (!runningCommandAlerts.containsKey((Command)joinPoint.getThis())) {
            var command = (Command)joinPoint.getThis();
            var alertType = command instanceof BaseMaintainerCommand<?, ?> ? Alert.AlertType.kWarning : Alert.AlertType.kInfo;
            var alert = new Alert("RunningCommands", command.getName(), alertType);
            alert.set(true);
            commandStartTimes.put(command, XTimer.getFPGATimestamp());
            runningCommandAlerts.put(command, alert);
        }
    }

    @Before("execution(* edu.wpi.first.wpilibj2.command.Command+.end(..))")
    public void clearCommandAlert(JoinPoint joinPoint) {
        if (runningCommandAlerts.containsKey((Command)joinPoint.getThis())) {
            var command = (Command)joinPoint.getThis();
            var wasInterrupted = (Boolean)joinPoint.getArgs()[0];
            completedAlertList.add(runningCommandAlerts.remove(command));
            var startTime = commandStartTimes.remove(command);
            var endTime = XTimer.getFPGATimestamp();
            logger.info("Command {} took {} seconds (interrupted: {})",
                    command.getName(), String.format("%.4f", endTime - startTime), wasInterrupted);
        }
    }
}
// CHECKSTYLE:ON
