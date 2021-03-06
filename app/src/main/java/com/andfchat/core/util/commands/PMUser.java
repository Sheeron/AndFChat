/*******************************************************************************
 *     This file is part of AndFChat.
 *
 *     AndFChat is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AndFChat is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AndFChat.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.andfchat.core.util.commands;

import android.content.Context;

import roboguice.event.EventManager;

import com.andfchat.R;
import com.andfchat.core.connection.handler.PrivateMessageHandler;
import com.andfchat.core.connection.handler.VariableHandler.Variable;
import com.andfchat.core.data.CharacterManager;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.Chatroom.ChatroomType;
import com.andfchat.core.data.FCharacter;
import com.andfchat.core.data.SessionData;
import com.andfchat.core.data.messages.ChatEntry;
import com.andfchat.core.data.messages.ChatEntryFactory;
import com.google.inject.Inject;

public class PMUser extends TextCommand {

    @Inject
    protected EventManager eventManager;
    @Inject
    protected SessionData sessionData;
    @Inject
    protected ChatEntryFactory entryFactory;
    @Inject
    protected Context context;

    public PMUser() {
        allowedIn = ChatroomType.values();
    }

    @Override
    public String getDescription() {
        return "*  /priv " + context.getString(R.string.command_description_priv);
    }

    @Override
    public boolean fitToCommand(String token) {
        return token.equals("/priv");
    }

    @Override
    public void runCommand(String token, String text) {
        if (text == null || text.length() == 0) {
            ChatEntry entry = entryFactory.getError(characterManager.findCharacter(CharacterManager.USER_SYSTEM), R.string.error_no_name_given);
            chatroomManager.addMessage(chatroomManager.getActiveChat(), entry);
            return;
        }

        FCharacter flistChar = characterManager.findCharacter(text, false);

        if (flistChar == null) {
            ChatEntry entry = entryFactory.getError(characterManager.findCharacter(CharacterManager.USER_SYSTEM), R.string.error_name_not_found, new Object[]{text});
            chatroomManager.addMessage(chatroomManager.getActiveChat(), entry);
        } else {
            Chatroom chatroom;
            if (!chatroomManager.hasOpenPrivateConversation(flistChar)) {
                int maxTextLength = sessionData.getIntVariable(Variable.priv_max);
                chatroom = PrivateMessageHandler.openPrivateChat(chatroomManager, flistChar, maxTextLength, sessionData.getSessionSettings().showAvatarPictures());

                if (flistChar.getStatusMsg() != null && flistChar.getStatusMsg().length() > 0) {
                    chatroomManager.addMessage(chatroom, entryFactory.getStatusInfo(flistChar));
                }
            } else {
                chatroom = chatroomManager.getPrivateChatFor(flistChar);
            }

            chatroomManager.setActiveChat(chatroom);
            eventManager.fire(chatroom);
        }
    }
}
