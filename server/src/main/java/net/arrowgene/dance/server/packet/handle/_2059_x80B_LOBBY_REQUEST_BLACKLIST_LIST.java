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

import net.arrowgene.dance.library.models.character.SocialEntry;
import net.arrowgene.dance.server.client.DanceClient;
import net.arrowgene.dance.server.DanceServer;
import net.arrowgene.dance.server.packet.PacketType;
import net.arrowgene.dance.server.packet.ReadPacket;
import net.arrowgene.dance.server.packet.SendPacket;

import java.util.ArrayList;
import java.util.List;

public class _2059_x80B_LOBBY_REQUEST_BLACKLIST_LIST extends HandlerBase {


    public _2059_x80B_LOBBY_REQUEST_BLACKLIST_LIST(DanceServer server) {
        super(server);
    }

    @Override
    public SendPacket[] handle(ReadPacket packet, DanceClient client) {

        List<SocialEntry> blackList = client.getBlackList();

        SendPacket answerPacket = new SendPacket(PacketType.LOBBY_REQUEST_BLACKLIST_LIST);
        answerPacket.addInt32(0);
        answerPacket.addInt32(blackList.size());
        for (SocialEntry blackListed : blackList) {
            answerPacket.addStringNulTerminated(blackListed.getSocialName());
        }
        answerPacket.addByte(0);

        client.sendPacket(answerPacket);

        return null;
    }
}