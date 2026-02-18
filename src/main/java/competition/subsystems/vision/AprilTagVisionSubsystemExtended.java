package competition.subsystems.vision;

//import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.vision.april_tag.AprilTagVisionIO;
import xbot.common.subsystems.vision.april_tag.AprilTagVisionIOFactory;
import xbot.common.subsystems.vision.april_tag.AprilTagVisionSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Optional;

@Singleton
public class AprilTagVisionSubsystemExtended extends AprilTagVisionSubsystem {
    HashMap<Pose2d, Integer> aprilTagIDHashMap = new HashMap<>();
    private final AprilTagFieldLayout aprilTagFieldLayout;
    public final CameraInfo[] cameras;

    @Inject
    public AprilTagVisionSubsystemExtended(PropertyFactory pf,
                                           AprilTagFieldLayout fieldLayout, XCameraElectricalContract contract,
                                           AprilTagVisionIOFactory visionIOFactory) {
        super(pf, fieldLayout, contract, visionIOFactory);
        this.cameras = contract.getCameraInfo();
        // TODO update these landmarks if needed, see https://github.com/Team488/TeamXbot2026/pull/11
        //
        // Note: flipped april tag IDs across the y-midpoint of the field for blue alliance
        // map both blue and red alliance poses
        // aprilTagIDHashMap.put(PoseSubsystem.convertBluetoRed(Landmarks.BlueCloseLeftAlgae), 6);
        // aprilTagIDHashMap.put(PoseSubsystem.convertBluetoRed(Landmarks.BlueCloseAlgae), 7);
        // aprilTagIDHashMap.put(PoseSubsystem.convertBluetoRed(Landmarks.BlueCloseRightAlgae), 8);
        // aprilTagIDHashMap.put(PoseSubsystem.convertBluetoRed(Landmarks.BlueFarLeftAlgae), 11);
        // aprilTagIDHashMap.put(PoseSubsystem.convertBluetoRed(Landmarks.BlueFarAlgae), 10);
        // aprilTagIDHashMap.put(PoseSubsystem.convertBluetoRed(Landmarks.BlueFarRightAlgae), 9);
        // aprilTagIDHashMap.put(Landmarks.BlueCloseLeftAlgae, 19);
        // aprilTagIDHashMap.put(Landmarks.BlueCloseAlgae, 18);
        // aprilTagIDHashMap.put(Landmarks.BlueCloseRightAlgae, 17);
        // aprilTagIDHashMap.put(Landmarks.BlueFarLeftAlgae, 20);
        // aprilTagIDHashMap.put(Landmarks.BlueFarAlgae, 21);
        // aprilTagIDHashMap.put(Landmarks.BlueFarRightAlgae, 22);

        aprilTagFieldLayout = fieldLayout;
    }

    public Translation2d getRobotRelativeLocationOfBestDetectedAprilTag(int cameraToUse) {
        Translation3d data = getLatestTargetObservation(cameraToUse).cameraToTarget().getTranslation().rotateBy(
                getCameraPosition(cameraToUse).getRotation()
        );

        return new Translation2d(data.getX(), data.getY());
    }

    public Optional<Translation2d> getRobotRelativeLocationOfAprilTag(int cameraToUse, int tagId) {
        var observation = getTargetObservation(cameraToUse, tagId);
        return observation.map(obs -> {
            Translation3d data = obs.cameraToTarget().getTranslation().rotateBy(
                    getCameraPosition(cameraToUse).getRotation()
            );

            return new Translation2d(data.getX(), data.getY());
        });
    }

    /**
     * Returns the Pose3d of an april tag, given that it exists
     * @param targetAprilTagID we are getting the pose for
     * @return the Pose3d (AKA position) of the tag
     */
    public Optional<Pose3d> getAprilTagFieldOrientedPose(int targetAprilTagID) {
        return aprilTagFieldLayout.getTagPose(targetAprilTagID);
    }

    public boolean reefAprilTagCameraHasCorrectTarget(int targetAprilTagID) {
        AprilTagVisionIO.TargetObservation targetObservation = getLatestTargetObservation(0);
        return targetObservation.fiducialId() == targetAprilTagID;
    }

    public boolean doesCameraBestObservationHaveAprilTagId(int cameraToUse, int targetAprilTagID) {
        AprilTagVisionIO.TargetObservation targetObservation = getLatestTargetObservation(cameraToUse);
        return targetObservation.fiducialId() == targetAprilTagID;
    }

    public int getTargetAprilTagID(Pose2d targetReefFacePose) {
        return aprilTagIDHashMap.get(targetReefFacePose);
    }

    public int getClosestReefTagIdFromTranslation(Translation2d translation) {
        double minDistance = Double.MAX_VALUE;
        int closestTagId = -1;
        for (Pose2d reefFacePose : aprilTagIDHashMap.keySet()) {
            if (translation.getDistance(reefFacePose.getTranslation()) < minDistance) {
                minDistance = translation.getDistance(reefFacePose.getTranslation());
                closestTagId = aprilTagIDHashMap.get(reefFacePose);
            }
        }

        return closestTagId;
    }

    public boolean areAllCamerasConnected() {
        for (int i = 0; i < cameras.length; i++) {
            if (!isCameraConnected(i)) {
                return false;
            }
        }
        return true;
    }
}
