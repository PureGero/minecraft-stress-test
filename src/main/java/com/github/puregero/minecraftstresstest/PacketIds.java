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

            //client outbound
            public static final int
                    DISCONNECT = 0x1B,  //ok +1
                    KEEP_ALIVE = 0x24,  //ok +1
                    PING = 0x33,  //ok +1
                    SYNCHRONIZE_PLAYER_POSITION = 0x3E,  //ok +2
                    RESOURCE_PACK = 0x43,  //ok +3
                    SET_HEALTH = 0x5B;  //ok
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
                    LOGIN_START = 0x00,
            LOGIN_ACKNOWLEDGED = 0x03;
        }

        public static final class Play {
            private Play() {
            }

            //server outbound
            public static final int
                    CONFIRM_TELEPORTATION = 0x00, //same
                    CLIENT_RESPAWN = 0x08,  //....
                    CLIENT_INFORMATION = 0x00, //ok
                    KEEP_ALIVE = 0x15, //ok
                    SET_PLAYER_POSITION_AND_ROTATION = 0x18, //ok
                    PONG = 0x24,  //ok
                    RESOURCE_PACK = 0x28; //ok
        }

    }

}
