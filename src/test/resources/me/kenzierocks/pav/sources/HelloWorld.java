package me.kenzierocks.pav.sources;

import com.google.auto.value.AutoValue;
import me.kenzierocks.pav.Packet;

@AutoValue
public abstract class HelloWorld implements Packet {

    @AutoValue.Builder
    public static abstract class Creator implements Packet.Creator<HelloWorld> {

        abstract Creator helloWorld(String value);

    }

    public abstract String getHelloWorld();

}