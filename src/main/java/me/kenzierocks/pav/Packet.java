package me.kenzierocks.pav;

import java.io.DataOutputStream;

public interface Packet {

    interface Creator<T extends Packet> {

        // Causes errors about not being related to any property
        // T readPacket(DataInputStream inputStream);

        T create();

    }

    void writePacket(DataOutputStream outputStream);

}
