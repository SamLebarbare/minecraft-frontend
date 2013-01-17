/*
 * Copyright (c) 2013, Samuel Loup. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package rcon;

import rcon.exception.BadRcon;
import rcon.exception.ResponseEmpty;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

/**
 * Minecragt RCON Lib based on oscahie (aka PiTaGoRaS) Source Engine based games
 */
public class MinecraftRcon {

    final static int SERVERDATA_EXECCOMMAND = 2;
    final static int SERVERDATA_AUTH = 3;
    final static int SERVERDATA_RESPONSE_VALUE = 0;
    final static int SERVERDATA_AUTH_RESPONSE = 2;

    final static int RESPONSE_TIMEOUT = 2000;
    final static int MULTIPLE_PACKETS_TIMEOUT = 300;

    static Socket rconSocket = null;
    static InputStream in = null;
    static OutputStream out = null;


    /**
     * Send the RCON command to the game server (must have been previously authed with the correct rcon_password)
     *
     * @param ipStr     The IP (as a String) of the machine where the RCON command will go.
     * @param port      The port of the machine where the RCON command will go.
     * @param password  The RCON password.
     * @param command   The RCON command (without the rcon prefix).
     * @return The reponse text from the server after trying the RCON command.
     * @throws IOException  if there was an input/output problem
     */
    public static String send(String ipStr, int port, String password, String command) throws BadRcon, ResponseEmpty, IOException {
        return send(ipStr, port, password, command, 0);
    }
    
    /**
     * Send the RCON command to the game server (must have been previously authed with the correct rcon_password)
     *
     * @param ipStr     The IP (as a String) of the machine where the RCON command will go.
     * @param port      The port of the machine where the RCON command will go.
     * @param password  The RCON password.
     * @param command   The RCON command (without the rcon prefix).
     * @param localPort The port of the local machine to use for sending out the RCON request.
     * @return The reponse text from the server after trying the RCON command.
     * @throws IOException  if there was an input/output problem
     */
    public static String send(String ipStr, int port, String password, String command, int localPort) throws BadRcon, ResponseEmpty, IOException {
        String response = "";

            rconSocket = new Socket();

            InetAddress addr = getLocalHostLANAddress();
            byte[] ipAddr = addr.getAddress();
            InetAddress inetLocal = InetAddress.getByAddress(ipAddr);
            
            rconSocket.bind(new InetSocketAddress(inetLocal, localPort));
            rconSocket.connect(new InetSocketAddress(ipStr, port), 1000);

            out = rconSocket.getOutputStream();
            in = rconSocket.getInputStream();

            rconSocket.setSoTimeout(RESPONSE_TIMEOUT);

            if (rcon_auth(password)) {
                // We are now authed
                ByteBuffer[] resp = sendCommand(command);
                // Close socket handlers, we don't need them more
                out.close(); in.close(); rconSocket.close();
                if (resp != null) {
                    response = assemblePackets(resp);
                    if (response.length() == 0) {
                        throw new ResponseEmpty();
                    }
                }
            }
            else {
                throw new BadRcon();
            }

        return response;
    }


    private static ByteBuffer[] sendCommand(String command) throws IOException {

        byte[] request = constructPacket(2, SERVERDATA_EXECCOMMAND, command);

        ByteBuffer[] resp = new ByteBuffer[128];
        int i = 0;
            out.write(request);
            resp[i] = receivePacket();  // First and maybe the unique response packet
            try {
                // We don't know how many packets will return in response, so we'll
                // read() the socket until TimeoutException occurs.
                rconSocket.setSoTimeout(MULTIPLE_PACKETS_TIMEOUT);
                while (true) {
                    resp[++i] = receivePacket();
                }
            } catch (SocketTimeoutException e) {
                // No more packets in the response, go on
                return resp;
            }
    }


    private static byte[] constructPacket(int id, int cmdtype, String s1) {

        ByteBuffer p = ByteBuffer.allocate(s1.length() + 16);
        p.order(ByteOrder.LITTLE_ENDIAN);

        // length of the packet
        p.putInt(s1.length() + 12);
        // request id
        p.putInt(id);
        // type of command
        p.putInt(cmdtype);
        // the command itself
        p.put(s1.getBytes());
        // two null bytes at the end
        p.put((byte) 0x00);
        p.put((byte) 0x00);
        // null string2 (see Source protocol)
        p.put((byte) 0x00);
        p.put((byte) 0x00);

        return p.array();
    }

    private static ByteBuffer receivePacket() throws IOException {

        ByteBuffer p = ByteBuffer.allocate(4120);
        p.order(ByteOrder.LITTLE_ENDIAN);

        byte[] length = new byte[4];

        if (in.read(length, 0, 4) != 4) {
        	return null;
        }
        
        // Now we've the length of the packet, let's go read the bytes
        p.put(length);
        int i = 0;
        while (i < p.getInt(0)) {
            p.put((byte) in.read());
            i++;
        }
        return p;
    }


    private static String assemblePackets(ByteBuffer[] packets) {
    // Return the text from all the response packets together

        String response = "";

        for (int i = 0; i < packets.length; i++) {
            if (packets[i] != null) {
                response = response.concat(new String(packets[i].array(), 12, packets[i].position()-14));
            }
        }
        return response;
    }


    private static boolean rcon_auth(String rcon_password) throws IOException {

        byte[] authRequest = constructPacket(0, SERVERDATA_AUTH, rcon_password);
        System.out.println("AuthRequest:"+authRequest.toString());
        ByteBuffer response = ByteBuffer.allocate(64);
            out.write(authRequest);
            response = receivePacket(); // junk response packet
            //response = receivePacket();

            // Lets see if the received request_id is leet enougth ;)
            if ((response.getInt(4) == 0) && (response.getInt(8) == SERVERDATA_AUTH_RESPONSE)) {
                return true;
            }

        return false;
    }
    
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

}
