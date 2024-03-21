package com.github.puregero.minecraftstresstest;

public final class PacketIds {
    private PacketIds() {
    }

    public static final class Clientbound {
        private Clientbound() {
        }

        public static final class Login {
            private Login() {
            }

            public static final int
                    DISCONNECT = 0x00,
                    LOGIN_SUCCESS = 0x02,
                    SET_COMPRESSION = 0x03;
        }

        public static final class Play {
            private Play() {
            }

            public static final int
                    DISCONNECT = 0x1A,  //conv
                    KEEP_ALIVE = 0x23,  //conv
                    PING = 0x32,  //conv
                    SYNCHRONIZE_PLAYER_POSITION = 0x3C,  //conv
                    RESOURCE_PACK = 0x40,  //conv
                    SET_HEALTH = 0x57;  //conv
        }

    }

    public static final class Serverbound {
        private Serverbound() {
        }

        public static final class Handshaking {
            private Handshaking() {
            }

            public static final int
                    HANDSHAKE = 0x00;
        }

        public static final class Login {
            private Login() {
            }

            public static final int
                    LOGIN_START = 0x00;
        }

        public static final class Play {
            private Play() {
            }

            public static final int
                    CONFIRM_TELEPORTATION = 0x00, //same
                    CLIENT_RESPAWN = 0x07,  //conv
                    CLIENT_INFORMATION = 0x08, //conv
                    KEEP_ALIVE = 0x12, //conv
                    SET_PLAYER_POSITION_AND_ROTATION = 0x15, //conv
                    PONG = 0x20,  //conv
                    RESOURCE_PACK = 0x24; //same
        }

    }

}
