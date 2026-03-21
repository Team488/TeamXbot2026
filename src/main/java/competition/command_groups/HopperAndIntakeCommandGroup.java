package competition.command_groups;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployOscillateForCollectionCommand;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class HopperAndIntakeCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public HopperAndIntakeCommandGroup(HopperRollerSubsystem hopperRoller,
                                       CollectorIntakeCommand fuelIntakeCommand,
                                       IntakeDeployOscillateForCollectionCommand intakeOscillationCommand) {
        addCommands(fuelIntakeCommand, hopperRoller.getIntakeCommand(), intakeOscillationCommand);
    }
}
