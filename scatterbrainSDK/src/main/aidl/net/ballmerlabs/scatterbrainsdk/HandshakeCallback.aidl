package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.HandshakeResult;

interface HandshakeCallback {
    oneway void onError(in String error);
    oneway void onResult(in HandshakeResult identity);
}