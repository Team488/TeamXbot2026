package competition.subsystems.intake_deploy.commands;

import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.operator_interface.OperatorCommandMap;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseCommand;
import xbot.common.properties.PropertyFactory;

public class IntakeDeployControlledClosingOscillate extends BaseCommand {

    IntakeDeploySubsystem intakeDeploySubsystem;
    ShooterSubsystem shooterSubsystem;

    public IntakeDeployControlledClosingOscillate(IntakeDeploySubsystem intakeDeploySubsystem,
                                                  PropertyFactory pf,
                                                  ShooterSubsystem shooterSubsystem,
                                                  OperatorCommandMap operatorCommandMap) {
//        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
    }

    @Override
    public void initialize() {
    
        //map a button to command map to try
    }

    @Override
    public void execute() {
        if (ShooterSubsystem.isReadyToFire) {
            intakeDeploySubsystem.intakeDeployGoUp();
        }        //only close intake when shooting
    }
}
