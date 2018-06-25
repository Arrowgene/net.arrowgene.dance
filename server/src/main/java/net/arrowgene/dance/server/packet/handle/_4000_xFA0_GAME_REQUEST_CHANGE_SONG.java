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

package net.arrowgene.dance.server.packet.handle;

import net.arrowgene.dance.server.client.DanceClient;
import net.arrowgene.dance.server.DanceServer;
import net.arrowgene.dance.server.packet.Packet;
import net.arrowgene.dance.server.packet.PacketType;
import net.arrowgene.dance.server.packet.ReadPacket;
import net.arrowgene.dance.server.packet.SendPacket;
import net.arrowgene.dance.server.packet.builder.RoomPacket;


public class _4000_xFA0_GAME_REQUEST_CHANGE_SONG extends HandlerBase {

    public _4000_xFA0_GAME_REQUEST_CHANGE_SONG(DanceServer server) {
        super(server);
    }

    @Override
    public SendPacket[] handle(ReadPacket packet, DanceClient client) {
        int song = packet.getInt16();
        int sogn2 = packet.getInt16();
        byte difficult = packet.getByte();
        byte difficult2 = packet.getByte();
        byte other = packet.getByte();

        // wenn client Host im Raum ist
        if(client.isHostInRoom()) {
            client.getRoom().setSong(this.server.getSongManager().getSong(song));
            client.getRoom().setSongDifficult(difficult);
            client.getRoom().setSongDifficult2(difficult2);


            Packet answerPacket = RoomPacket.getInstance().getChangeSongPacket(client.getRoom(), other);
            client.getRoom().sendPacket(answerPacket);
        }

        return null;


        // if (host == client) {
    }
}
