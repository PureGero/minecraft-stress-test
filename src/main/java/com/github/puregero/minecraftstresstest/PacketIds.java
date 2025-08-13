package com.github.puregero.minecraftstresstest;

public final class PacketIds {
    private PacketIds() {
    }

    //what we receive
    public static final class Clientbound {
        private Clientbound() {
        }

        public static final class Login {
            private Login() {
            }

            public static final int
                    DISCONNECT = 0x00,          //login_disconnect
                    ENCRYPTION_REQUEST = 0x01,  //hello
                    LOGIN_SUCCESS = 0x02,       //login_finished
                    SET_COMPRESSION = 0x03;     //login_compression
        }

        public static final class Configuration {
            private Configuration() {
            }

            public static final int
                    DISCONNECT = 0x02,          //disconnect
                    FINISH_CONFIGURATION = 0x03,//finish_configuration
                    KEEP_ALIVE = 0x04,          //KEEP_ALIVE
                    PING = 0x05;                //PING
        }

        public static final class Play {
            private Play() {
            }

            //client outbound
            public static final int
                    DISCONNECT = 0x1D,                   //DISCONNECT
                    KEEP_ALIVE = 0x27,                   //KEEP_ALIVE
                    PING = 0x37,                         //PING         Play 	Client
                    SYNCHRONIZE_PLAYER_POSITION = 0x42,  //player_position
                    RESOURCE_PACK = 0x4B,                //resource_pack_push
                    SET_HEALTH = 0x62;                   //SET_HEALTH
        }

    }

    //what we send to server
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
                    LOGIN_START = 0x00,         //hello
                    LOGIN_ACKNOWLEDGED = 0x03;  //login_acknowledged
        }

        public static final class Configuration {
            private Configuration() {
            }

            public static final int
                    CLIENT_INFORMATION = 0x00,  //client_information
                    FINISH_CONFIGURATION = 0x03,//finish_configuration
                    KEEP_ALIVE = 0x04,          //keep_alive
                    PONG = 0x05,                //pong
                    KNOWN_PACKS = 0x07;         //select_known_packs
        }

        public static final class Play {
            private Play() {
            }

            public static final int
                    CONFIRM_TELEPORTATION = 0x00,           //accept_teleportation
                    CLIENT_RESPAWN = 0x0A,                  //client_command
                    KEEP_ALIVE = 0x1A,                      //keep_alive
                    SET_PLAYER_POSITION_AND_ROTATION = 0x1D,//move_player_pos_rot
                    PONG = 0x2B,                            //PONG    Play 	Server
                    RESOURCE_PACK = 0x2F;                   //RESOURCE_PACK
        }

    }

}