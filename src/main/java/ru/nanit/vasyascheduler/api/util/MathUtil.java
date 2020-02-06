package ru.nanit.vasyascheduler.api.util;

public final class MathUtil {

    public MathUtil(){}

    public static int max(int... values){
        if(values.length == 0){
            return 0;
        }

        int max = values[0];

        for (int i : values){
            if(i > max){
                max = i;
            }
        }

        return max;
    }

    public static long max(long... values){
        if(values.length == 0){
            return 0;
        }

        long max = values[0];

        for (long i : values){
            if(i > max){
                max = i;
            }
        }

        return max;
    }

    public static float max(float... values){
        if(values.length == 0){
            return 0;
        }

        float max = values[0];

        for (float i : values){
            if(i > max){
                max = i;
            }
        }

        return max;
    }

    public static double max(double... values){
        if(values.length == 0){
            return 0;
        }

        double max = values[0];

        for (double i : values){
            if(i > max){
                max = i;
            }
        }

        return max;
    }

    public static byte max(byte... values){
        if(values.length == 0){
            return 0;
        }

        byte max = values[0];

        for (byte i : values){
            if(i > max){
                max = i;
            }
        }

        return max;
    }

    public static int min(int... values){
        if(values.length == 0){
            return 0;
        }

        int min = values[0];

        for (int i : values){
            if(i < min){
                min = i;
            }
        }

        return min;
    }

    public static long min(long... values){
        if(values.length == 0){
            return 0;
        }

        long min = values[0];

        for (long i : values){
            if(i < min){
                min = i;
            }
        }

        return min;
    }

    public static float min(float... values){
        if(values.length == 0){
            return 0;
        }

        float min = values[0];

        for (float i : values){
            if(i < min){
                min = i;
            }
        }

        return min;
    }

    public static double min(double... values){
        if(values.length == 0){
            return 0;
        }

        double min = values[0];

        for (double i : values){
            if(i < min){
                min = i;
            }
        }

        return min;
    }

    public static byte min(byte... values){
        if(values.length == 0){
            return 0;
        }

        byte min = values[0];

        for (byte i : values){
            if(i < min){
                min = i;
            }
        }

        return min;
    }
}
