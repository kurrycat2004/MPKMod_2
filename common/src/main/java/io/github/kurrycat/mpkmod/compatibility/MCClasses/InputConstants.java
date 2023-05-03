package io.github.kurrycat.mpkmod.compatibility.MCClasses;

@SuppressWarnings("unused")
public class InputConstants {
    public static final int KEY_0 = 48;
    public static final int KEY_1 = 49;
    public static final int KEY_2 = 50;
    public static final int KEY_3 = 51;
    public static final int KEY_4 = 52;
    public static final int KEY_5 = 53;
    public static final int KEY_6 = 54;
    public static final int KEY_7 = 55;
    public static final int KEY_8 = 56;
    public static final int KEY_9 = 57;
    public static final int KEY_A = 65;
    public static final int KEY_B = 66;
    public static final int KEY_C = 67;
    public static final int KEY_D = 68;
    public static final int KEY_E = 69;
    public static final int KEY_F = 70;
    public static final int KEY_G = 71;
    public static final int KEY_H = 72;
    public static final int KEY_I = 73;
    public static final int KEY_J = 74;
    public static final int KEY_K = 75;
    public static final int KEY_L = 76;
    public static final int KEY_M = 77;
    public static final int KEY_N = 78;
    public static final int KEY_O = 79;
    public static final int KEY_P = 80;
    public static final int KEY_Q = 81;
    public static final int KEY_R = 82;
    public static final int KEY_S = 83;
    public static final int KEY_T = 84;
    public static final int KEY_U = 85;
    public static final int KEY_V = 86;
    public static final int KEY_W = 87;
    public static final int KEY_X = 88;
    public static final int KEY_Y = 89;
    public static final int KEY_Z = 90;
    public static final int KEY_F1 = 290;
    public static final int KEY_F2 = 291;
    public static final int KEY_F3 = 292;
    public static final int KEY_F4 = 293;
    public static final int KEY_F5 = 294;
    public static final int KEY_F6 = 295;
    public static final int KEY_F7 = 296;
    public static final int KEY_F8 = 297;
    public static final int KEY_F9 = 298;
    public static final int KEY_F10 = 299;
    public static final int KEY_F11 = 300;
    public static final int KEY_F12 = 301;
    public static final int KEY_F13 = 302;
    public static final int KEY_F14 = 303;
    public static final int KEY_F15 = 304;
    public static final int KEY_F16 = 305;
    public static final int KEY_F17 = 306;
    public static final int KEY_F18 = 307;
    public static final int KEY_F19 = 308;
    public static final int KEY_F20 = 309;
    public static final int KEY_F21 = 310;
    public static final int KEY_F22 = 311;
    public static final int KEY_F23 = 312;
    public static final int KEY_F24 = 313;
    public static final int KEY_F25 = 314;
    public static final int KEY_NUMLOCK = 282;
    public static final int KEY_NUMPAD0 = 320;
    public static final int KEY_NUMPAD1 = 321;
    public static final int KEY_NUMPAD2 = 322;
    public static final int KEY_NUMPAD3 = 323;
    public static final int KEY_NUMPAD4 = 324;
    public static final int KEY_NUMPAD5 = 325;
    public static final int KEY_NUMPAD6 = 326;
    public static final int KEY_NUMPAD7 = 327;
    public static final int KEY_NUMPAD8 = 328;
    public static final int KEY_NUMPAD9 = 329;
    public static final int KEY_NUMPADCOMMA = 330;
    public static final int KEY_NUMPADENTER = 335;
    public static final int KEY_NUMPADEQUALS = 336;
    public static final int KEY_DOWN = 264;
    public static final int KEY_LEFT = 263;
    public static final int KEY_RIGHT = 262;
    public static final int KEY_UP = 265;
    public static final int KEY_ADD = 334;
    public static final int KEY_APOSTROPHE = 39;
    public static final int KEY_BACKSLASH = 92;
    public static final int KEY_COMMA = 44;
    public static final int KEY_EQUALS = 61;
    public static final int KEY_GRAVE = 96;
    public static final int KEY_LBRACKET = 91;
    public static final int KEY_MINUS = 45;
    public static final int KEY_MULTIPLY = 332;
    public static final int KEY_PERIOD = 46;
    public static final int KEY_RBRACKET = 93;
    public static final int KEY_SEMICOLON = 59;
    public static final int KEY_SLASH = 47;
    public static final int KEY_SPACE = 32;
    public static final int KEY_TAB = 258;
    public static final int KEY_LALT = 342;
    public static final int KEY_LCONTROL = 341;
    public static final int KEY_LSHIFT = 340;
    public static final int KEY_LWIN = 343;
    public static final int KEY_RALT = 346;
    public static final int KEY_RCONTROL = 345;
    public static final int KEY_RSHIFT = 344;
    public static final int KEY_RWIN = 347;
    public static final int KEY_RETURN = 257;
    public static final int KEY_ESCAPE = 256;
    public static final int KEY_BACKSPACE = 259;
    public static final int KEY_DELETE = 261;
    public static final int KEY_END = 269;
    public static final int KEY_HOME = 268;
    public static final int KEY_INSERT = 260;
    public static final int KEY_PAGEDOWN = 267;
    public static final int KEY_PAGEUP = 266;
    public static final int KEY_CAPSLOCK = 280;
    public static final int KEY_PAUSE = 284;
    public static final int KEY_SCROLLLOCK = 281;
    public static final int KEY_PRINTSCREEN = 283;
    /*public static final int PRESS = 1;
    public static final int RELEASE = 0;
    public static final int REPEAT = 2;
    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_MIDDLE = 2;
    public static final int MOUSE_BUTTON_RIGHT = 1;
    public static final int MOD_CONTROL = 2;
    public static final int CURSOR = 208897;
    public static final int CURSOR_DISABLED = 212995;
    public static final int CURSOR_NORMAL = 212993;*/

    public static boolean isHoldingShift(int modifiers) {
        return (modifiers & 1) == 1;
    }

    public static int convert(int lwjglKey) {
        int key = 0;

        if (lwjglKey >= 0x02 && lwjglKey <= 0x0A)
            return lwjglKey - 0x02 + KEY_1;

        switch (lwjglKey) {
            case 0x01:
                return KEY_ESCAPE;
            case 0x0B:
                return KEY_0;
            case 0x0C:
                return KEY_MINUS;
            case 0x0D:
                return KEY_EQUALS;
            case 0x0E:
                return KEY_BACKSPACE;
            case 0x0F:
                return KEY_TAB;
            case 0x10:
                return KEY_Q;
            case 0x11:
                return KEY_W;
            case 0x12:
                return KEY_E;
            case 0x13:
                return KEY_R;
            case 0x14:
                return KEY_T;
            case 0x15:
                return KEY_Y;
            case 0x16:
                return KEY_U;
            case 0x17:
                return KEY_I;
            case 0x18:
                return KEY_O;
            case 0x19:
                return KEY_P;
            case 0x1A:
                return KEY_LBRACKET;
            case 0x1B:
                return KEY_RBRACKET;
            case 0x1C:
                return KEY_RETURN; /* Enter on main keyboard */
            case 0x1D:
                return KEY_LCONTROL;
            case 0x1E:
                return KEY_A;
            case 0x1F:
                return KEY_S;
            case 0x20:
                return KEY_D;
            case 0x21:
                return KEY_F;
            case 0x22:
                return KEY_G;
            case 0x23:
                return KEY_H;
            case 0x24:
                return KEY_J;
            case 0x25:
                return KEY_K;
            case 0x26:
                return KEY_L;
            case 0x27:
                return KEY_SEMICOLON;
            case 0x28:
                return KEY_APOSTROPHE;
            case 0x29:
                return KEY_GRAVE; /* accent grave */
            case 0x2A:
                return KEY_LSHIFT;
            case 0x2B:
                return KEY_BACKSLASH;
            case 0x2C:
                return KEY_Z;
            case 0x2D:
                return KEY_X;
            case 0x2E:
                return KEY_C;
            case 0x2F:
                return KEY_V;
            case 0x30:
                return KEY_B;
            case 0x31:
                return KEY_N;
            case 0x32:
                return KEY_M;
            case 0x33:
                return KEY_COMMA;
            case 0x34:
                return KEY_PERIOD; /* . on main keyboard */
            case 0x35:
                return KEY_SLASH; /* / on main keyboard */
            case 0x36:
                return KEY_RSHIFT;
            case 0x37:
                return KEY_MULTIPLY; /* * on numeric keypad */
            case 0x38:
                return KEY_LALT; /* left Alt */
            case 0x39:
                return KEY_SPACE;
            case 0x3A:
                return KEY_CAPSLOCK;
            case 0x3B:
                return KEY_F1;
            case 0x3C:
                return KEY_F2;
            case 0x3D:
                return KEY_F3;
            case 0x3E:
                return KEY_F4;
            case 0x3F:
                return KEY_F5;
            case 0x40:
                return KEY_F6;
            case 0x41:
                return KEY_F7;
            case 0x42:
                return KEY_F8;
            case 0x43:
                return KEY_F9;
            case 0x44:
                return KEY_F10;
            case 0x45:
                return KEY_NUMLOCK;
            case 0x46:
                return KEY_SCROLLLOCK; /* Scroll Lock */
            case 0x47:
                return KEY_NUMPAD7;
            case 0x48:
                return KEY_NUMPAD8;
            case 0x49:
                return KEY_NUMPAD9;
            case 0x4A:
                return KEY_MINUS; /* - on numeric keypad */
            case 0x4B:
                return KEY_NUMPAD4;
            case 0x4C:
                return KEY_NUMPAD5;
            case 0x4D:
                return KEY_NUMPAD6;
            case 0x4E:
                return KEY_ADD; /* + on numeric keypad */
            case 0x4F:
                return KEY_NUMPAD1;
            case 0x50:
                return KEY_NUMPAD2;
            case 0x51:
                return KEY_NUMPAD3;
            case 0x52:
                return KEY_NUMPAD0;
            case 0x53:
                return KEY_NUMPADCOMMA; /* . on numeric keypad */
            case 0x57:
                return KEY_F11;
            case 0x58:
                return KEY_F12;
            case 0x64:
                return KEY_F13; /*                     (NEC PC98) */
            case 0x65:
                return KEY_F14; /*                     (NEC PC98) */
            case 0x66:
                return KEY_F15; /*                     (NEC PC98) */
            case 0x67:
                return KEY_F16; /* Extended Function keys - (Mac) */
            case 0x68:
                return KEY_F17;
            case 0x69:
                return KEY_F18;
            /*case 0x70:
                return KEY_KANA;*/ /* (Japanese keyboard)            */
            case 0x71:
                return KEY_F19; /* Extended Function keys - (Mac) */
            /*case 0x79:
                return KEY_CONVERT;*/ /* (Japanese keyboard)            */
            /*case 0x7B:
                return KEY_NOCONVERT;*/ /* (Japanese keyboard)            */
            /*case 0x7D:
                return KEY_YEN;*/ /* (Japanese keyboard)            */
            case 0x8D:
                return KEY_NUMPADEQUALS; /* = on numeric keypad (NEC PC98) */
            /*case 0x90:
                return KEY_CIRCUMFLEX;*/ /* (Japanese keyboard)            */
            /*case 0x91:
                return KEY_AT;*/ /*                     (NEC PC98) */
            /*case 0x92:
                return KEY_COLON;*/ /*                     (NEC PC98) */
            /*case 0x93:
                return KEY_UNDERLINE;*/ /*                     (NEC PC98) */
            /*case 0x94:
                return KEY_KANJI;*/ /* (Japanese keyboard)            */
            /*case 0x95:
                return KEY_STOP;*/ /*                     (NEC PC98) */
            /*case 0x96:
                return KEY_AX;*/ /*                     (Japan AX) */
            /*case 0x97:
                return KEY_UNLABELED;*/ /*                        (J3100) */
            case 0x9C:
                return KEY_NUMPADENTER; /* Enter on numeric keypad */
            case 0x9D:
                return KEY_RCONTROL;
            /*case 0xA7:
                return KEY_SECTION;*/ /* Section symbol (Mac) */
            case 0xB3:
                return KEY_NUMPADCOMMA; /* , on numeric keypad (NEC PC98) */
            case 0xB5:
                return KEY_SLASH; /* / on numeric keypad */
           /* case 0xB7:
                return KEY_SYSRQ;*/
            case 0xB8:
                return KEY_RALT; /* right Alt */
           /*case 0xC4:
                return KEY_FUNCTION;*/ /* Function (Mac) */
            case 0xC5:
                return KEY_PAUSE; /* Pause */
            case 0xC7:
                return KEY_HOME; /* Home on arrow keypad */
            case 0xC8:
                return KEY_UP; /* UpArrow on arrow keypad */
            case 0xC9:
                return KEY_PAGEUP; /* PgUp on arrow keypad */
            case 0xCB:
                return KEY_LEFT; /* LeftArrow on arrow keypad */
            case 0xCD:
                return KEY_RIGHT; /* RightArrow on arrow keypad */
            case 0xCF:
                return KEY_END; /* End on arrow keypad */
            case 0xD0:
                return KEY_DOWN; /* DownArrow on arrow keypad */
            case 0xD1:
                return KEY_PAGEDOWN; /* PgDn on arrow keypad */
            case 0xD2:
                return KEY_INSERT; /* Insert on arrow keypad */
            case 0xD3:
                return KEY_DELETE; /* Delete on arrow keypad */
            /*case 0xDA:
                return KEY_CLEAR;*/ /* Clear key (Mac) */
            case 0xDB:
                return KEY_LWIN; /* Left Windows/Option key */

            case 0xDC:
                return KEY_RWIN; /* Right Windows/Option key */

            /*case 0xDD:
                return KEY_APPS;*/ /* AppMenu key */
            /*case 0xDE:
                return KEY_POWER;*/
            /*case 0xDF:
                return KEY_SLEEP;*/
        }
        return key;
    }
}
