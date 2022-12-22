package com.github.puregero.minecraftstresstest;

public final class PacketIds {
	private PacketIds() {}

	public static final class Clientbound {
		private Clientbound() {}

		public static final class Login {
			private Login() {}
			public static final int
					DISCONNECT = 0x00,
					LOGIN_SUCCESS = 0x02,
					SET_COMPRESSION = 0x03;
		}

		public static final class Play {
			private Play() {}
			public static final int
					DISCONNECT = 0x19,
					KEEP_ALIVE = 0x20,
					PING = 0x2F,
					SYNCHRONIZE_PLAYER_POSITION = 0x39,
					RESOURCE_PACK = 0x3D;
		}

	}

	public static final class Serverbound {
		private Serverbound() {}

		public static final class Handshaking {
			private Handshaking() {}
			public static final int
					HANDSHAKE = 0x00;
		}

		public static final class Login {
			private Login() {}
			public static final int
					LOGIN_START = 0x00;
		}

		public static final class Play {
			private Play() {}
			public static final int
					CONFIRM_TELEPORTATION = 0x00,
					CLIENT_INFORMATION = 0x08,
					KEEP_ALIVE = 0x12,
					SET_PLAYER_POSITION_AND_ROTATION = 0x15,
					PONG = 0x20,
					RESOURCE_PACK = 0x24;
		}

	}

}
