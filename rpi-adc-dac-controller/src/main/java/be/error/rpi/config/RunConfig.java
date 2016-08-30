package be.error.rpi.config;

import static com.pi4j.io.i2c.I2CBus.BUS_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;

import be.error.rpi.dac.i2c.I2CCommunicator;
import be.error.rpi.knx.KnxConnectionFactory;

/**
 * @author Koen Serneels
 */
public class RunConfig {

	private static final Logger logger = LoggerFactory.getLogger(RunConfig.class);

	public static String LOCAL_IP;

	private final String LOXONE_IP = "192.168.0.5";
	private final IndividualAddress LOXONE_IA;
	private final int LOXONE_PORT = 6000;

	private final String KNX_IP = "192.168.0.6";
	private final int KNX_PORT = 3671;

	private final int DAC_LISTEN_PORT = 8000;

	private I2CBus bus;
	private I2CCommunicator i2CCommunicator;

	private static RunConfig runConfig;

	private RunConfig(String localIp) {
		LOCAL_IP = localIp;
		try {
			LOXONE_IA = new IndividualAddress("1.1.250");
		} catch (KNXException knxException) {
			throw new RuntimeException(knxException);
		}
	}

	public static RunConfig getInstance() {
		if (runConfig == null) {
			throw new IllegalStateException("Initialize first");
		}
		return runConfig;
	}

	public static void initialize(String localIp) {
		runConfig = new RunConfig(localIp);
		runConfig.initialize();
	}

	private void initialize() {
		try {
			bus = I2CFactory.getInstance(BUS_1);
			i2CCommunicator = new I2CCommunicator();
			i2CCommunicator.start();
		} catch (Exception e) {
			logger.error("Could not start I2CCommunicator", e);
			throw new RuntimeException(e);
		}
	}

	//Lazy singleton initialization: only initialize when needed
	private static class KnxConnection {
		private static KnxConnectionFactory knxConnectionFactory = new KnxConnectionFactory(runConfig.getKnxIp(), runConfig.getKnxPort(), runConfig.getLocalIp());
	}

	public String getLocalIp() {
		return LOCAL_IP;
	}

	public String getLoxoneIp() {
		return LOXONE_IP;
	}

	public int getLoxonePort() {
		return LOXONE_PORT;
	}

	public String getKnxIp() {
		return KNX_IP;
	}

	public int getKnxPort() {
		return KNX_PORT;
	}

	public int getDacListenPort() {
		return DAC_LISTEN_PORT;
	}

	public IndividualAddress getLoxoneIa() {
		return LOXONE_IA;
	}

	public I2CBus getBus() {
		return bus;
	}

	public KnxConnectionFactory getKnxConnectionFactory() {
		return KnxConnection.knxConnectionFactory;
	}

	public I2CCommunicator getI2CCommunicator() {
		return i2CCommunicator;
	}
}