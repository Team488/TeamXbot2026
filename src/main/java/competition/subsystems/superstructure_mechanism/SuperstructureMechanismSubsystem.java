package competition.subsystems.superstructure_mechanism;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Responsible for rendering a Mechanism2d representing the hood and intake deploy as perceived by
 * the robot code.
 */
@Singleton
public class SuperstructureMechanismSubsystem extends BaseSubsystem {
    final HoodSubsystem hoodSubsystem;
    final IntakeDeploySubsystem intakeDeploySubsystem;
    final SuperstructureMechanism mechanism;

    @Inject
    public SuperstructureMechanismSubsystem(HoodSubsystem hoodSubsystem,
                                            IntakeDeploySubsystem intakeDeploySubsystem) {
        this.hoodSubsystem = hoodSubsystem;
        this.intakeDeploySubsystem = intakeDeploySubsystem;
        this.mechanism = new SuperstructureMechanism();
    }

    @Override
    public void periodic() {
        mechanism.setHoodNormalizedPosition(hoodSubsystem.getCurrentValue());
        mechanism.setIntakeAngle(intakeDeploySubsystem.getCurrentValue());
        mechanism.setIntakeExtendedPosition(intakeDeploySubsystem.extendedPosition.get());
        aKitLog.record("SuperstructureMechanism", mechanism.getMechanism());
    }
}
