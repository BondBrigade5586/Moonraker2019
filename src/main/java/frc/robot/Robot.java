/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.*;
import com.ctre.phoenix.motorcontrol.InvertType;

import com.ctre.phoenix.motorcontrol.NeutralMode;
// import com.ctre.phoenix.motorcontrol.DemandType;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GamepadBase;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  WPI_TalonSRX m_frontLeft = new WPI_TalonSRX(0); // 0 port on practice chassis
  WPI_TalonSRX m_frontRight = new WPI_TalonSRX(3); // 1 port on practice chassis
  VictorSPX m_rightSlave = new VictorSPX(0);
  VictorSPX m_leftSlave = new VictorSPX(1);

  WPI_TalonSRX m_intake = new WPI_TalonSRX(2); // 2 port on practice chassis
  WPI_TalonSRX m_lift = new WPI_TalonSRX(1); // 3 port on practice chassis

  DoubleSolenoid m_extend = new DoubleSolenoid(0, 1);
  DoubleSolenoid m_open = new DoubleSolenoid(2, 3);
  Compressor m_compressor = new Compressor(0);

  private final DifferentialDrive drive = new DifferentialDrive(m_frontLeft, m_frontRight);

  Joystick _flightStick = new Joystick(0);
  XboxController _gamepad = new XboxController(1);

  private boolean m_LimelightHasValidTarget = false;
  private double m_LimelightDriveCommand = 0.0;
  private double m_LimelightSteerCommand = 0.0;

  private double factor = 1.0;

  boolean toggleExtensionOn = false;
  boolean toggleExtensionPressed = false;
  boolean toggleOpenOn = false;
  boolean toggleOpenPressed = false;

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {

    System.out.println("I'm JamesTimed");

    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    m_frontLeft.configFactoryDefault();
    m_intake.configFactoryDefault();
    m_frontRight.configFactoryDefault();
    m_lift.configFactoryDefault();
    m_rightSlave.configFactoryDefault();
    m_leftSlave.configFactoryDefault();

    m_rightSlave.follow(m_frontRight);
    m_leftSlave.follow(m_frontLeft);

    m_frontLeft.setNeutralMode(NeutralMode.Brake);
    m_frontRight.setNeutralMode(NeutralMode.Brake);

    m_frontLeft.setInverted(false);
    m_leftSlave.setInverted(InvertType.FollowMaster);
    m_frontRight.setInverted(true);
    m_rightSlave.setInverted(InvertType.FollowMaster);
    m_lift.setInverted(true);

    m_frontLeft.set(ControlMode.PercentOutput, 0);
    m_frontRight.set(ControlMode.PercentOutput, 0);

    drive.setRightSideInverted(false);

    UsbCamera camera = CameraServer.getInstance().startAutomaticCapture(0); 
    camera.setResolution(320, 240);
    camera.setExposureManual(40);
    camera.setFPS(15);
    UsbCamera camera2 = CameraServer.getInstance().startAutomaticCapture(1);
    camera2.setResolution(320, 240);
    camera2.setExposureManual(40);
    camera2.setFPS(15);

    m_lift.setSelectedSensorPosition(0);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {

    double forward = -1 * _flightStick.getY();
    double turn = _flightStick.getTwist();
    forward = Deadband(forward);
    turn = Deadband(turn);

    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
    default:
      // Put default auto code here
      break;
    }
    drive.arcadeDrive(forward * factor, turn);
    m_lift.set(_gamepad.getRawAxis(5));
    if (_gamepad.getPOV() == 180) {
      m_intake.set(-0.5);
    } else if (_gamepad.getPOV() == 0) {
      m_intake.set(1.0);
    } else {
      m_intake.set(0);
    }
  }

  @Override
  public void teleopInit() {
    System.out.print("teleopInit");
    SmartDashboard.putString("Status", "Teleop has been initiated.");
    drive.arcadeDrive(0, 0);
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    NetworkTableInstance.getDefault().getTable("");
    NetworkTableEntry entry = NetworkTableInstance.getDefault().getEntry("/CameraSelection");

    updateExtensionToggle();
    updateOpenToggle();
 
    // DRIVING VARIABLES
    double forward = -1 * _flightStick.getY();
    double turn = _flightStick.getTwist();
    forward = Deadband(forward);
    turn = Deadband(turn);

    if (_flightStick.getRawButtonPressed(8)) {
      factor = 1.0;
    } else if (_flightStick.getRawButtonPressed(10)) {
      factor = 0.75;
    } else if (_flightStick.getRawButtonPressed(12)) {
      factor = 0.5;
    }

    // VISION VARIABLES
    boolean auto = _flightStick.getRawButton(2);

    // *******************************************
    // Pneumatics
    // *******************************************
    m_compressor.setClosedLoopControl(true);

    if (toggleExtensionPressed) {
      m_extend.set(DoubleSolenoid.Value.kReverse);
    } else {
      m_extend.set(DoubleSolenoid.Value.kForward);
    }

    if (toggleOpenPressed) {
      m_open.set(DoubleSolenoid.Value.kForward);
    } else { 
      m_open.set(DoubleSolenoid.Value.kReverse);
    }

    // *******************************************
    // Camera
    // *******************************************


    // *******************************************
    // Drive
    // *******************************************

    // Reversie
    if (_flightStick.getRawButton(5)) {
      m_frontLeft.setInverted(true);
      m_frontRight.setInverted(false);
      SmartDashboard.putString("Front:", "Cannon");
    } else if (_flightStick.getRawButton(3)) {
      m_frontLeft.setInverted(false);
      m_frontRight.setInverted(true);
      SmartDashboard.putString("Front:", "Ball Intake");
    }

    // Move it
    if (auto) {
      if (m_LimelightHasValidTarget) {
        drive.arcadeDrive(-m_LimelightDriveCommand, m_LimelightSteerCommand);
      } else {
        drive.arcadeDrive(0.0, 0.0);
      }
    } else {
      drive.arcadeDrive(forward * factor, turn);
    }

    SmartDashboard.putString("Drive", "Forward: " + forward + " || Turn: " + turn + " || Factor: " + factor);

    // *******************************************
    // Limelight Magic
    // *******************************************
    Update_Limelight_Tracking();

    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");
    NetworkTableEntry tv = table.getEntry("tv");

    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double area = ta.getDouble(0.0);
    double sight = tv.getDouble(0.0);

    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightArea", area);
    SmartDashboard.putNumber("LimelightSight", sight);

    // Toggle Limelight mode
    if (_gamepad.getAButton()) {
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(0);
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode").setNumber(0);
    } else if (_gamepad.getBButton()) {
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode").setNumber(1);
    }

    // *******************************************
    // Run the intake
    // *******************************************
    if (_gamepad.getPOV() == 180) {
      m_intake.set(-0.5);
    } else if (_gamepad.getPOV() == 0) {
      m_intake.set(1.0);
    } else {
      m_intake.set(0);
    }

    // *******************************************
    // Handle lift
    // *******************************************
    if (_gamepad.getXButton()) {
      SmartDashboard.putNumber("Lift", 0);
      m_lift.set(_gamepad.getRawAxis(5));
      m_lift.setSelectedSensorPosition(0);
    } else {
      // Check postition
      // Dont run motor if over high-limit or under low-limit
      m_lift.set(_gamepad.getRawAxis(5));
      double liftOutput = m_lift.getSelectedSensorPosition();
      SmartDashboard.putNumber("Lift", liftOutput);
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

  /** Deadband 5 percent, used on the gamepad */
  double Deadband(double value) {
    if (value >= +0.5)
      return value;
    if (value <= -0.5)
      return value;
    return 0;
  }

  public void Update_Limelight_Tracking() {
    // These numbers must be tuned for your Robot! Be careful!
    final double STEER_K = 0.03; // how hard to turn toward the target
    final double DRIVE_K = 0.26; // how hard to drive fwd toward the target
    final double DESIRED_TARGET_AREA = 13.0; // 13 // Area of the target when the robot reaches the wall
    final double MAX_DRIVE = 0.65; // Simple speed limit so we don't drive too fast

    double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
    double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
    double ty = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
    double ta = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);

    if (tv < 1.0) {
      m_LimelightHasValidTarget = false;
      m_LimelightDriveCommand = 0.0;
      m_LimelightSteerCommand = 0.0;
      return;
    }

    m_LimelightHasValidTarget = true;

    // Start with proportional steering
    double steer_cmd = tx * STEER_K;
    m_LimelightSteerCommand = steer_cmd;

    // try to drive forward until the target area reaches our desired area
    double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;

    // don't let the robot drive too fast into the goal
    if (drive_cmd > MAX_DRIVE) {
      drive_cmd = MAX_DRIVE;
    }
    m_LimelightDriveCommand = drive_cmd;
  }

  public void updateExtensionToggle() {
    if (_gamepad.getBumperPressed(Hand.kLeft)) {
      if (!toggleExtensionPressed) {
        toggleExtensionOn = !toggleExtensionOn;
        toggleExtensionPressed = true;
      } else {
        toggleExtensionPressed = false;
      }
    }
  }
  public void updateOpenToggle() {
    if (_gamepad.getBumperPressed(Hand.kRight)) {
      if (!toggleOpenPressed) {
        toggleOpenOn = !toggleOpenOn;
        toggleOpenPressed = true;
      } else {
        toggleOpenPressed = false;
      }
    }
  }
  public void spit() {
    System.out.println("Just for you Alex.");
  }
}
