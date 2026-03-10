package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class IntakeDeployAutoCalibrateCommandFactory {
    private final Logger log = LogManager.getLogger(IntakeDeployAutoCalibrateCommandFactory.class);
    private final IntakeDeploySubsystem subsystem;
    private final Provider<IntakeDeployWaitForEndStopOrCurrentLimitCommand> waitCommandProvider;
    private final Provider<IntakeDeployExtendWithoutPidCommand> extendCommandProvider;
    private final DoubleProperty currentLimit;
    private final DoubleProperty autoCalibratePower;

    @Inject
    public IntakeDeployAutoCalibrateCommandFactory(PropertyFactory pf,
                                                   IntakeDeploySubsystem subsystem,
                                                   Provider<IntakeDeployWaitForEndStopOrCurrentLimitCommand> waitCommandProvider,
                                                   Provider<IntakeDeployExtendWithoutPidCommand> extendCommandProvider) {
        this.subsystem = subsystem;
        this.waitCommandProvider = waitCommandProvider;
        this.extendCommandProvider = extendCommandProvider;

        pf.setPrefix(this.getClass().getSimpleName());
        this.currentLimit = pf.createPersistentProperty("CurrentLimitAmps", 10.0);
        this.autoCalibratePower = pf.createPersistentProperty("AutoCalibratePower", -0.1);
    }

    public Command create() {
        var waitCommand = waitCommandProvider.get()
                .setIsTouchingSensorSupplier(subsystem::isTouchingIntakeDeploy)
                .setCurrentLimitSupplier(currentLimit::get);
        var extendCommand = extendCommandProvider.get().setPower(autoCalibratePower::get);

        return new ConditionalCommand(
            new InstantCommand(() -> log.info("IntakeDeploy is already calibrated, skipping auto-calibration.")),
            new ParallelDeadlineGroup(waitCommand, extendCommand)
                    .withTimeout(Seconds.of(2)),
            subsystem::isCalibrated
        );
    }
}
