package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseSetpointCommand;

import java.util.function.Supplier;

import javax.inject.Inject;

public class HoodSetCommand extends BaseSetpointCommand {
    private final HoodSubsystem hood;
    private double ratio;
    private boolean usingCustomGoal = false;
    private Supplier<Double> ratioSupplier = null;

    @Inject
    public HoodSetCommand(HoodSubsystem hood) {
        super(hood);
        this.hood = hood;
        this.ratio = hood.servoTargetNormalized.get();
    }

    public void setTargetRatio(double ratio) {
        this.ratio = ratio;
        this.usingCustomGoal = true;
    }

    public void setTargetRatio(Supplier<Double> ratioSupplier) {
        this.ratioSupplier = ratioSupplier;
        this.usingCustomGoal = true;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!this.usingCustomGoal) {
            this.ratio = hood.servoTargetNormalized.get();
        } else if (this.ratioSupplier != null) {
            this.ratio = this.ratioSupplier.get();
        }

        log.info("Setting hood target to {}", ratio);
        this.hood.setTargetValue(ratio);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
