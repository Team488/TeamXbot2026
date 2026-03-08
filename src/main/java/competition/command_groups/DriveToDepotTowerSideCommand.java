package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.subsystems.pose.GameField;

public class DriveToDepotTowerSideCommand extends SwerveSimpleBezierCommand {

    public DriveToDepotTowerSideCommand(DriveSubsystem drive, PoseSubsystem pose,
                                        PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                        XSwerveDriveElectricalContract electricalContract,
                                        RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning, GameField gamefield,
                                        AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);

        this.drive = drive;
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.gamefield = gamefield;
        this.robotRadius = electricalContract.getRadiusOfRobot();
        this.autoLandmarks = autoLandmarks;
    }
}
