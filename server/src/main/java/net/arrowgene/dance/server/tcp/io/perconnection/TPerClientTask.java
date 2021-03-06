/*
 * This file is part of net.arrowgene.dance.
 *
 * net.arrowgene.dance is a server implementation for the game "Dance! Online".
 * Copyright (C) 2013-2018  Sebastian Heinz (github: sebastian-heinz)
 * Copyright (C) 2013-2018  Daniel Neuendorf
 *
 * Github: https://github.com/Arrowgene/net.arrowgene.dance
 * Web: https://arrowgene.net
 *
 * net.arrowgene.dance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * net.arrowgene.dance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.arrowgene.dance.server.tcp.io.perconnection;


import net.arrowgene.dance.log.LogType;
import net.arrowgene.dance.log.Logger;
import net.arrowgene.dance.server.ServerLogger;
import net.arrowgene.dance.server.packet.Packet;
import net.arrowgene.dance.server.packet.PacketType;
import net.arrowgene.dance.server.packet.ReadPacket;
import net.arrowgene.dance.server.tcp.TcpServer;
import net.arrowgene.dance.server.tcp.io.TcpClientIO;
import net.arrowgene.dance.server.tcp.io.TcpServerIO;

import java.io.IOException;
import java.io.InputStream;


public class TPerClientTask implements Runnable {


    private TcpClientIO client;
    private TcpServerIO tcpServerIO;
    private ServerLogger logger;

    TPerClientTask(TcpServerIO tcpServerIO, ServerLogger logger, TcpClientIO client) {
        this.logger = logger;
        this.tcpServerIO = tcpServerIO;
        this.client = client;
    }

    @Override
    public void run() {
        this.tcpServerIO.clientConnected(this.client);
        while (this.client.getAlive()) {

            int readBytes = -1;
            byte[] header = new byte[Packet.HEADER_SIZE];

            try {
                InputStream inStream = client.getSocket().getInputStream();
                readBytes = inStream.read(header, 0, Packet.HEADER_SIZE);
            } catch (IOException e) {
                if (TcpServerIO.isSocketClosedException(e)) {
                    this.client.setAlive(false);
                } else {
                    this.logger.writeLog(e, this.client);
                    this.disconnectCauseError();
                }
            }

            if (readBytes == Packet.HEADER_SIZE) {

                short packetSize = (short) (((header[1] & 0xff) << 8) | (header[0] & 0xff));

                if (packetSize > 0 && packetSize < TcpServer.MAX_PACKET_SIZE) {

                    short packetId = (short) (((header[3] & 0xff) << 8) | (header[2] & 0xff));

                    int bytesToRead = packetSize - Packet.HEADER_SIZE;
                    byte[] body = new byte[bytesToRead];

                    try {
                        InputStream inStream = client.getSocket().getInputStream();
                        readBytes = inStream.read(body, 0, bytesToRead);
                    } catch (IOException e) {
                        if (TcpServerIO.isSocketClosedException(e)) {
                            this.client.setAlive(false);
                        } else {
                            this.logger.writeLog(e, this.client);
                            this.disconnectCauseError();
                        }
                    }

                    if (readBytes == bytesToRead) {
                        PacketType packetType = PacketType.getType(packetId);
                        if (packetType != null) {
                            ReadPacket readPacket = new ReadPacket(packetType, body);
                            this.tcpServerIO.packetReceived(this.client, readPacket);
                        } else {
                            this.logger.writeUnknownPacketLog(packetId, packetSize, body, client);
                        }
                    } else if (readBytes < 0) {
                        this.client.setAlive(false);
                    } else {
                        this.logger.writeLog(LogType.WARNING, "Reported packet size(" + bytesToRead +
                            ") does not match read bytes from stream(" + readBytes + ")", this.client);
                    }
                } else {
                    this.logger.writeLog(LogType.WARNING, "Packet of size:" + packetSize
                        + " exceeding maximum:" + TcpServer.MAX_PACKET_SIZE
                        + " received", client);
                }
            } else if (readBytes < 0) {
                this.client.setAlive(false);
            } else {
                this.logger.writeLog(LogType.WARNING, "Packet header size(" + Packet.HEADER_SIZE +
                    ") does not match read bytes from stream(" + readBytes + ")", this.client);
            }
        }
        this.tcpServerIO.clientDisconnected(this.client);
    }

    private void disconnectCauseError() {
        this.logger.writeLog(LogType.ERROR, "Client encountered error, disconnecting...", this.client);
        client.setAlive(false);
    }
}
