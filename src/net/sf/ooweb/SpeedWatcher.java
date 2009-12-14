package net.sf.ooweb;

public class SpeedWatcher{

    private int delay;
    private static final int MAX = 10;
    private long slots[];
    private long total;
    private boolean printed;

    public SpeedWatcher(int printSpeed){
        delay = printSpeed;
        slots = new long[MAX];
        total = 0;
        printed = false;
    }

    public SpeedWatcher(){
        this(10000000);
    }

    public void update(int bytes){
        int slot = (int)((long)(System.currentTimeMillis() / (delay / MAX)) % MAX);
        /*
        System.out.println("delay / MAX is " + (delay / MAX));
        System.out.println("xxx " + (System.currentTimeMillis() / (delay / MAX)));
        System.out.println("slot is " + slot);
        */
        slots[slot] += bytes;
        if (slot == 0 && !printed){
            print();
            for (int i = 0; i < MAX; i++){
                slots[i] = 0;
            }
            printed = true;
        } else if (slot > 0){
            printed = false;
        }

        total += bytes;
    }

    public long totalBytes(){
        return total;
    }

    public double currentSpeed(){
        long current = 0;
        for (long x : slots){
            current += x;
        }
        return (double)current / ((double) delay / 1000.0);
    }

    public void print(){
        /* nothing */
    }
}
