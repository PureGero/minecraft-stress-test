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
					DISCONNECT = 0x17,
					KEEP_ALIVE = 0x1F,
					PING = 0x2E,
					SYNCHRONIZE_PLAYER_POSITION = 0x38,
					RESOURCE_PACK = 0x3C;
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
					CLIENT_INFORMATION = 0x07,
					KEEP_ALIVE = 0x11,
					SET_PLAYER_POSITION_AND_ROTATION = 0x14,
					PONG = 0x1F,
					RESOURCE_PACK = 0x24;
		}

	}

}
