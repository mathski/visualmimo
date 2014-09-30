package visualmimo.com.visualmimo.persistence;

public class FrameOps {

    //For Subtraction
    public static Frame intensify(Frame frame) {
        byte[] raw = frame.getRaw();

        boolean increase = true;
        for(int i = 0; i < raw.length; i++){
            if(increase) {
                if(!(raw[i] + 10 > 255)) {
                    raw[i] += 10;
                    increase = false;
                }
            } else {
                increase = true;
            }
        }

        return new Frame(raw, frame.getWidth(), frame.getHeight());
    }

    //For division
    public static Frame scale(Frame frame) {
        byte[] raw = frame.getRaw();

        boolean increase = true;
        for(int i = 0; i < raw.length; i++){
            if(increase) {
                    raw[i] *= 10;
                    increase = false;
            } else {
                increase = true;
            }
        }

        return new Frame(raw, frame.getWidth(), frame.getHeight());
    }

    public static Frame frameSubtraction(Frame x, Frame y) {

        byte[] normal =  x.getRaw();
        byte[] intensified = y.getRaw();

        int len = intensified.length;
        if(normal.length < intensified.length)
            len = normal.length;

        byte[] sub = new byte[len];

        for(int i = 0 ; i < len; i++){
            sub[i] = (byte)( (intensified[i]) - normal[i]);
        }

        return new Frame(sub, x.getWidth(), x.getHeight());
    }

    public static Frame frameDivision(Frame x, Frame y) {
        byte[] normal =  x.getRaw();
        byte[] intensified = y.getRaw();

        int len = intensified.length;
        if(normal.length < intensified.length)
            len = normal.length;

        byte[] sub = new byte[len];
        for(int i = 0 ; i < len; i++){
            //Increase normal by 1 to avoid zero division
            sub[i] = (byte)((intensified[i]/(normal[i]+1))*10);
        }

        return new Frame(sub, x.getWidth(), x.getHeight());
    }
}
