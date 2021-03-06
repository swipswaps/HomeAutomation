/*-
 * #%L
 * Home Automation
 * %%
 * Copyright (C) 2016 - 2017 Koen Serneels
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package be.error.rpi.knx;

import static be.error.rpi.config.RunConfig.LOCAL_IP;
import static be.error.rpi.knx.UdpChannelCommand.fromString;
import static java.net.InetAddress.getByName;
import static java.util.Collections.synchronizedList;
import static org.apache.commons.lang3.ArrayUtils.remove;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpChannel extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(UdpChannel.class);

	private final List<UdpChannelCallback> udpChannelCallbacks = synchronizedList(new ArrayList<>());
	private final DatagramSocket clientSocket;

	public UdpChannel(int port, UdpChannelCallback... udpChannelCallbacks) throws Exception {
		addUdpChannelCallback(udpChannelCallbacks);
		clientSocket = new DatagramSocket(port, getByName(LOCAL_IP));
	}

	@Override
	public void run() {
		String s = null;
		try {
			logger.debug("UdpChannel on port " + clientSocket.getPort() + " started.");

			while (true) {
				byte b[] = new byte[256];
				DatagramPacket receivePacket = new DatagramPacket(b, b.length);
				clientSocket.receive(receivePacket);
				s = new String(b, "UTF8").trim();
				logger.debug("UdpChannel received " + s);
				String[] split = StringUtils.split(s, "|");
				UdpChannelCommand udpChannelCommand = fromString(split[0]);
				synchronized (udpChannelCallbacks) {
					this.udpChannelCallbacks.stream().filter(cb -> cb.command() == udpChannelCommand).forEach(cb -> {
						try {
							cb.callBack(StringUtils.join(remove(split, 0)));
						} catch (Exception e) {
							logger.error("UdpChannel on port " + clientSocket.getPort() + " with command " + cb.command().toString(), e);
						}
					});
				}
			}
		} catch (Exception e) {
			logger.error("UdpChannel on port " + clientSocket.getPort() + " with raw command " + s, e);
		}
	}

	public void addUdpChannelCallback(UdpChannelCallback... udpChannelCallbacks) {
		if (udpChannelCallbacks != null) {
			logger.debug("Adding UdpChannelCallbacks " + udpChannelCallbacks);
			CollectionUtils.addAll(this.udpChannelCallbacks, udpChannelCallbacks);
			logger.debug("UdpChannelCallbacks: " + this.udpChannelCallbacks);
		}
	}

	public interface UdpChannelCallback {

		UdpChannelCommand command();

		void callBack(String s) throws Exception;
	}
}
