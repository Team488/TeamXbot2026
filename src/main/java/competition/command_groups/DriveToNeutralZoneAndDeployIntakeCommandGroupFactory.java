package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.ppl.PathPlanner;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class DriveToNeutralZoneAndDeployIntakeCommandGroupFactory {
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider;
    private final Provider<CollectorIntakeCommand> collectorIntakeCommandProvider;
    private final IntakeDeploySubsystem intakeDeploy;
    private final PathPlanner pathPlanner;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider,
            Provider<CollectorIntakeCommand> collectorIntakeCommandProvider,
            IntakeDeploySubsystem intakeDeploy,
            PathPlanner pathPlanner) {
        this.intakeDeployExtendCommandProvider = intakeDeployExtendCommandProvider;
        this.collectorIntakeCommandProvider = collectorIntakeCommandProvider;
        this.intakeDeploy = intakeDeploy;
        this.pathPlanner = pathPlanner;
    }

    public Command create() {
        var intakeDeployAndWait = Commands.sequence(
                intakeDeployExtendCommandProvider.get(),
                intakeDeploy.getWaitForAtGoalCommand()
        ).withName("IntakeDeployAndWaitCommand");

        var driveAcrossNeutralWhileIntaking = Commands.deadline(
                pathPlanner.driveAcrossMidNeutralZone(),
                collectorIntakeCommandProvider.get()
        ).withName("DriveAcrossNeutralWhileIntaking");

        return Commands.sequence(
                pathPlanner.driveToNeutralZoneForIntake(),
                intakeDeployAndWait,
                driveAcrossNeutralWhileIntaking
        ).withName("DriveToNeutralAndCollect");
    }

}
