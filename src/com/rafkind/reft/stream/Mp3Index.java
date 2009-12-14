package com.rafkind.reft.stream;

public class Mp3Index {

	private static final int DEFAULT = 96;

	private static final int V1 = 3;
	private static final int V2 = 2;

	private static final int L1 = 3;
	private static final int L2 = 2;
	private static final int L3 = 1;

	private static final int V1_L1 = (V1<<2) | L1;
	private static final int V1_L2 = (V1<<2) | L2;
	private static final int V1_L3 = (V1<<2) | L3;
	private static final int V2_L1 = (V2<<2) | L1;
	private static final int V2_L2 = (V2<<2) | L2;
	private static final int V2_L3 = (V2<<2) | L3;

	private Mp3Index(){
	}

	public static int lookup( int version, int layer, int rate ){
		switch ( (version << 2) | layer ){
			case V1_L1 : {
				switch ( rate ){
					case 0 : return DEFAULT;
					case 1 : return 32;
					case 2 : return 64;
					case 3 : return 96;
					case 4 : return 128;
					case 5 : return 160;
					case 6 : return 160;
					case 7 : return 224;
					case 8 : return 256;
					case 9 : return 288;
					case 10 : return 320;
					case 11 : return 352;
					case 12 : return 384;
					case 13 : return 416;
					case 14 : return 448;
					case 15 : return DEFAULT;
					default : return DEFAULT;
				}
			}
			case V1_L2 : {
				switch ( rate ){
					case 0 : return DEFAULT;
					case 1 : return 32;
					case 2 : return 48;
					case 3 : return 56;
					case 4 : return 64;
					case 5 : return 80;
					case 6 : return 96;
					case 7 : return 112;
					case 8 : return 128;
					case 9 : return 160;
					case 10 : return 192;
					case 11 : return 224;
					case 12 : return 256;
					case 13 : return 320;
					case 14 : return 384;
					case 15 : return DEFAULT; 
					default : return DEFAULT;
				}
			}
			case V1_L3 : {
				switch ( rate ){
					case 0 : return DEFAULT;
					case 1 : return 32;
					case 2 : return 40;
					case 3 : return 48;
					case 4 : return 56;
					case 5 : return 64;
					case 6 : return 80;
					case 7 : return 96;
					case 8 : return 112;
					case 9 : return 128;
					case 10 : return 160;
					case 11 : return 192;
					case 12 : return 224;
					case 13 : return 256;
					case 14 : return 320;
					case 15 : return DEFAULT;
					default : return DEFAULT;
				}
			}
			case V2_L1 : {
				switch ( rate ){
					case 0 : return DEFAULT;
					case 1 : return 32;
					case 2 : return 48;
					case 3 : return 56;
					case 4 : return 64;
					case 5 : return 80;
					case 6 : return 96;
					case 7 : return 112;
					case 8 : return 128;
					case 9 : return 144;
					case 10 : return 160;
					case 11 : return 176;
					case 12 : return 192;
					case 13 : return 224;
					case 14 : return 256;
					case 15 : return DEFAULT;
					default : return DEFAULT;
				}
			}
			case V2_L2 :
			case V2_L3 : {
				switch ( rate ){
					case 0 : return DEFAULT;
					case 1 : return 8;
					case 2 : return 16;
					case 3 : return 24;
					case 4 : return 32;
					case 5 : return 40;
					case 6 : return 48;
					case 7 : return 56;
					case 8 : return 64;
					case 9 : return 80;
					case 10 : return 96;
					case 11 : return 112;
					case 12 : return 128;
					case 13 : return 144;
					case 14 : return 160;
					case 15 : return DEFAULT;
					default : return DEFAULT;
				}
			}
			default : return DEFAULT;
		}
	}
}
