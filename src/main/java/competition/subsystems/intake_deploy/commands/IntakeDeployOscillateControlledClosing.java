package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeployOscillateControlledClosing extends BaseSetpointCommand {

    public IntakeDeploySubsystem intakeDeploySubsystem;
    public IntakeDeployExtendCommand intakeDeployExtendCommand;
    public AngleProperty increasingValue;

    public IntakeDeployOscillateControlledClosing(IntakeDeploySubsystem intakeDeploy,
                                                  PropertyFactory propertyFactory) {
        super(intakeDeploy);
        this.intakeDeploySubsystem = intakeDeploy;
        this.increasingValue = propertyFactory.createPersistentProperty("IncreasingValue", Degrees.of(0));
    }

    public void execute() {
        Angle retractLimit = Degrees.of(intakeDeploySubsystem.retractedPosition.get());
        Angle newValue = intakeDeploySubsystem.getCurrentValue().plus(increasingValue.get());

        if (newValue.gt(retractLimit)) {
            intakeDeploySubsystem.setTargetValue((retractLimit));
        }
        else {
            intakeDeploySubsystem.setTargetValue(newValue.minus(newValue));
            }
    }
}
