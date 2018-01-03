package jetson.vision.odometry

import jetson.vision.math.InterpolatingDouble
import jetson.vision.math.InterpolatingTreeMap
import jetson.vision.math.RigidTransform2D
import jetson.vision.math.Rotation2D

object FramesOfReference {
    private lateinit var fieldToVehicle: InterpolatingTreeMap<InterpolatingDouble, RigidTransform2D>
    private lateinit var vehicleVelocity: RigidTransform2D.Delta

    private const val OBSERVATION_BUFFER_SIZE = 100
    init {
        reset(0.0, RigidTransform2D())
    }

    fun reset(startTime: Double, initialFieldToVehicle: RigidTransform2D) {
        fieldToVehicle = InterpolatingTreeMap(OBSERVATION_BUFFER_SIZE)
        fieldToVehicle.put(InterpolatingDouble(startTime), initialFieldToVehicle)
        vehicleVelocity = RigidTransform2D.Delta(0.0, 0.0, 0.0)
    }

    fun fieldToVehicle(timestamp: Double): RigidTransform2D = fieldToVehicle.getInterpolated(InterpolatingDouble(timestamp))
    fun latestFieldToVehicle(): Map.Entry<InterpolatingDouble, RigidTransform2D> = fieldToVehicle.lastEntry()

    fun predictedFieldToVehicle(lookaheadTime: Double): RigidTransform2D {
        return latestFieldToVehicle()
                .value
                .transformBy(RigidTransform2D.fromVelocity(RigidTransform2D.Delta(
                        vehicleVelocity.deltaX * lookaheadTime,
                        vehicleVelocity.deltaY * lookaheadTime,
                        vehicleVelocity.deltaTheta * lookaheadTime)))
    }

    fun addFieldToVehicleObservation(timestamp: Double, observation: RigidTransform2D) {
        fieldToVehicle.put(InterpolatingDouble(timestamp), observation)
    }

    fun addObservations(timestamp: Double, fieldToVehicleObservation: RigidTransform2D, vel: RigidTransform2D.Delta) {
        addFieldToVehicleObservation(timestamp, fieldToVehicleObservation)
        vehicleVelocity = vel
    }

    fun generateFromSensors(leftEncoderDelta: Double, rightEncoderDelta: Double, currentGyro: Rotation2D): RigidTransform2D {
        val lastMeasurement = latestFieldToVehicle().value
        return Kinematics.integrateForwardKinematics(lastMeasurement, leftEncoderDelta, rightEncoderDelta, currentGyro)
    }
}